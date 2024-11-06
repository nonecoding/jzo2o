package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.utils.*;
import com.jzo2o.market.constants.TabTypeConstants;
import com.jzo2o.market.mapper.ActivityMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.SeizeCouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponWriteOffService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.jzo2o.market.constants.RedisConstants.RedisKey.ACTIVITY_CACHE_LIST;
import static com.jzo2o.market.enums.ActivityStatusEnum.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {
    private static final int MILLION = 1000000;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ICouponService couponService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;


    @Override
    public void preHeat() {
        //当前时间
        LocalDateTime now = DateUtils.now();

        //查询进行中还未到结束的优惠券活动， 1个月内待开始的优惠券活动
        /**
         select *
         from activity t
         where t.distribute_start_time < date_add(now(), INTERVAL 30 day))  and t.status in(1,2)
         order by t.distribute_start_time
         */
        List<Activity> list = lambdaQuery()
                .le(Activity::getDistributeStartTime, now.plusDays(30))//1个月内即将开始的
                .in(Activity::getStatus, Arrays.asList(NO_DISTRIBUTE.getStatus(), DISTRIBUTING.getStatus()))//查询待开始和进行中的
                .orderByAsc(Activity::getDistributeStartTime)
                .list();
        if (CollUtils.isEmpty(list)) {
            //防止缓存穿透
            list = new ArrayList<>();
        }
        // 2.数据转换
        List<SeizeCouponInfoResDTO> seizeCouponInfoResDTOS = BeanUtils.copyToList(list, SeizeCouponInfoResDTO.class);
        String seizeCouponInfoStr = JsonUtils.toJsonStr(seizeCouponInfoResDTOS);

        // 3.活动列表写入缓存
        redisTemplate.opsForValue().set(ACTIVITY_CACHE_LIST, seizeCouponInfoStr);


    }


    @Override
    public List<SeizeCouponInfoResDTO> queryForListFromCache(Integer tabType) {
        //从redis查询活动信息
        Object seizeCouponInfoStr = redisTemplate.opsForValue().get(ACTIVITY_CACHE_LIST);
        if (ObjectUtils.isNull(seizeCouponInfoStr)) {
            return CollUtils.emptyList();
        }
        //将json转为List
        List<SeizeCouponInfoResDTO> seizeCouponInfoResDTOS = JsonUtils.toList(seizeCouponInfoStr.toString(), SeizeCouponInfoResDTO.class);
        //根据tabType确定要查询的状态
        int queryStatus = tabType == TabTypeConstants.SEIZING ? DISTRIBUTING.getStatus() : NO_DISTRIBUTE.getStatus();
        //过滤数据，并设置剩余数量、实际状态
        List<SeizeCouponInfoResDTO> collect = seizeCouponInfoResDTOS.stream().filter(item -> queryStatus == getStatus(item.getDistributeStartTime(), item.getDistributeEndTime(), item.getStatus()))
                .peek(item -> {
                    //剩余数量
                    item.setRemainNum(item.getStockNum());
                    //状态
                    item.setStatus(queryStatus);
                }).collect(Collectors.toList());


        return collect;
    }

    @Override
    public ActivityInfoResDTO getActivityInfoByIdFromCache(Long id) {
        Object seizeCouponInfoStr = redisTemplate.opsForValue().get(ACTIVITY_CACHE_LIST);
        if (ObjectUtils.isNull(seizeCouponInfoStr)) {
            return null;
        }
        //将json转为List
        List<SeizeCouponInfoResDTO> seizeCouponInfoResDTOS = JsonUtils.toList(seizeCouponInfoStr.toString(), SeizeCouponInfoResDTO.class);
        //根据id查询
        SeizeCouponInfoResDTO seizeCouponInfoResDTO = seizeCouponInfoResDTOS.stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
        if (ObjectUtils.isNull(seizeCouponInfoResDTO)) {
            return null;
        }
        //设置剩余数量、实际状态
        seizeCouponInfoResDTO.setRemainNum(seizeCouponInfoResDTO.getStockNum());
        seizeCouponInfoResDTO.setStatus(getStatus(seizeCouponInfoResDTO.getDistributeStartTime(), seizeCouponInfoResDTO.getDistributeEndTime(), seizeCouponInfoResDTO.getStatus()));
        //将活动信息转为ActivityInfoResDTO
        ActivityInfoResDTO activityInfoResDTO = new ActivityInfoResDTO();
        BeanUtils.copyProperties(seizeCouponInfoResDTO, activityInfoResDTO);
        activityInfoResDTO.setReceiveNum(seizeCouponInfoResDTO.getTotalNum());
        activityInfoResDTO.setTotalNum(seizeCouponInfoResDTO.getTotalNum());
        activityInfoResDTO.setWriteOffNum(seizeCouponInfoResDTO.getTotalNum());

        return activityInfoResDTO;
    }

    /**
     * 获取状态，
     * 用于xxl或其他定时任务在高性能要求下无法做到实时状态
     *
     * @return
     */
    private int getStatus(LocalDateTime distributeStartTime, LocalDateTime distributeEndTime, Integer status) {
        if (NO_DISTRIBUTE.equals(status) &&
                distributeStartTime.isBefore(DateUtils.now()) &&
                distributeEndTime.isAfter(DateUtils.now())) {//待生效状态，实际活动已开始
            return DISTRIBUTING.getStatus();
        } else if (NO_DISTRIBUTE.equals(status) &&
                distributeEndTime.isBefore(DateUtils.now())) {//待生效状态，实际活动已结束
            return LOSE_EFFICACY.getStatus();
        } else if (DISTRIBUTING.equals(status) &&
                distributeEndTime.isBefore(DateUtils.now())) {//进行中状态，实际活动已结束
            return LOSE_EFFICACY.getStatus();
        }
        return status;
    }


    /**
     * 扣减库存
     *
     * @param id 活动id
     *           如果扣减库存失败抛出异常
     */
    public void deductStock(Long id) {
        boolean update = lambdaUpdate()
                .setSql("stock_num = stock_num-1")
                .eq(Activity::getId, id)
                .gt(Activity::getStockNum, 0)
                .update();
        if (!update) {
            throw new CommonException("扣减优惠券库存失败，活动id:" + id);
        }

    }

    @Override
    public void updateStatus() {
        //当前时间
        LocalDateTime now = DateUtils.now();
        //查询进行中还未到结束的优惠券活动， 1个月内待开始的优惠券活动
        /**
         select *
         from activity t
         where t.distribute_start_time < date_add(now(), INTERVAL 30 day))  and t.status in(1,2)
         order by t.distribute_start_time
         */
        List<Activity> list = lambdaQuery()
                .le(Activity::getDistributeStartTime, now.plusDays(30))//1个月内即将开始的
                .in(Activity::getStatus, Arrays.asList(NO_DISTRIBUTE.getStatus(), DISTRIBUTING.getStatus()))//查询待开始和进行中的
                .orderByAsc(Activity::getDistributeStartTime)
                .list();
        if (CollUtils.isEmpty(list)) {
            //防止缓存穿透
            list = new ArrayList<>();
        }
        // 2.数据转换
        List<SeizeCouponInfoResDTO> seizeCouponInfoResDTOS = BeanUtils.copyToList(list, SeizeCouponInfoResDTO.class);
        String seizeCouponInfoStr = JsonUtils.toJsonStr(seizeCouponInfoResDTOS);

        // 3.活动列表写入缓存
        redisTemplate.opsForValue().set(ACTIVITY_CACHE_LIST, seizeCouponInfoStr);
    }


}
