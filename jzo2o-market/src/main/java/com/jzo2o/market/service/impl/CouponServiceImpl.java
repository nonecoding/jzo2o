package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.DBException;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.*;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponWriteOff;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.market.utils.CouponUtils;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.redis.utils.RedisSyncQueueUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.jzo2o.common.constants.ErrorInfo.Code.SEIZE_COUPON_FAILD;
import static com.jzo2o.market.constants.RedisConstants.RedisKey.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    @Resource(name = "seizeCouponScript")
    private DefaultRedisScript<String> seizeCouponScript;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponUseBackService couponUseBackService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;


    @Override
    public void seizeCoupon(SeizeCouponReqDTO seizeCouponReqDTO) {
        // 1.校验活动开始时间或结束
        // 抢券时间
        ActivityInfoResDTO activity = activityService.getActivityInfoByIdFromCache(seizeCouponReqDTO.getId());
        LocalDateTime now = DateUtils.now();
        if (activity == null ||
                activity.getDistributeStartTime().isAfter(now)) {
            throw new CommonException(SEIZE_COUPON_FAILD, "活动未开始");
        }
        if (activity.getDistributeEndTime().isBefore(now)) {
            throw new CommonException(SEIZE_COUPON_FAILD, "活动已结束");
        }

        // 2.抢券准备
//         key: 抢券同步队列，资源库存,抢券列表
//         argv：抢券id,用户id
        int index = (int) (seizeCouponReqDTO.getId() % 10);
        // 同步队列redisKey
        String couponSeizeSyncRedisKey = RedisSyncQueueUtils.getQueueRedisKey(COUPON_SEIZE_SYNC_QUEUE_NAME, index);
        // 资源库存redisKey
        String resourceStockRedisKey = String.format(COUPON_RESOURCE_STOCK, index);
        // 抢券列表
        String couponSeizeListRedisKey = String.format(COUPON_SEIZE_LIST, activity.getId(), index);

        log.debug("seize coupon keys -> couponSeizeListRedisKey->{},resourceStockRedisKey->{},couponSeizeListRedisKey->{},seizeCouponReqDTO.getId()->{},UserContext.currentUserId():{}",
                couponSeizeListRedisKey, resourceStockRedisKey, couponSeizeListRedisKey, seizeCouponReqDTO.getId(), UserContext.currentUserId());
        // 3.抢券结果
        Object execute = redisTemplate.execute(seizeCouponScript, Arrays.asList(couponSeizeSyncRedisKey, resourceStockRedisKey, couponSeizeListRedisKey),
                seizeCouponReqDTO.getId(), UserContext.currentUserId());
        log.debug("seize coupon result : {}", execute);
        // 4.处理lua脚本结果
        if (execute == null) {
            throw new CommonException(SEIZE_COUPON_FAILD, "抢券失败");
        }
        long result = NumberUtils.parseLong(execute.toString());
        if (result > 0) {
            return;
        }
        if (result == -1) {
            throw new CommonException(SEIZE_COUPON_FAILD, "限领一张");
        }
        if (result == -2 || result == -4) {
            throw new CommonException(SEIZE_COUPON_FAILD, "已抢光!");
        }
        throw new CommonException(SEIZE_COUPON_FAILD, "抢券失败");
    }

    @Override
    public ActivityInfoResDTO getActivityInfoByIdFromCache(Long id) {
        // 1.从缓存中获取活动信息
        Object activityList = redisTemplate.opsForValue().get(ACTIVITY_CACHE_LIST);
        if (ObjectUtils.isNull(activityList)) {
            return null;
        }
        // 2.过滤指定活动信息
        List<ActivityInfoResDTO> list = JsonUtils.toList(activityList.toString(), ActivityInfoResDTO.class);
        if (CollUtils.isEmpty(list)) {
            return null;
        }
        // 3.过滤指定活动
        return list.stream()
                .filter(activityInfoResDTO -> activityInfoResDTO.getId().equals(id))
                .findFirst().orElse(null);
    }


    @Override
    public List<AvailableCouponsResDTO> getAvailable(BigDecimal totalAmount) {
        Long userId = UserContext.currentUserId();
        // 1.查询优惠券
        List<Coupon> coupons = lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .gt(Coupon::getValidityTime, DateUtils.now())
                .le(Coupon::getAmountCondition, totalAmount)
                .list();
        // 判空
        if (CollUtils.isEmpty(coupons)) {
            return new ArrayList<>();
        }

        // 2.组装数据计算优惠金额
        List<AvailableCouponsResDTO> collect = coupons.stream()
                .peek(coupon -> coupon.setDiscountAmount(CouponUtils.calDiscountAmount(coupon, totalAmount)))
                //过滤优惠金额大于0且小于订单金额的优惠券
                .filter(coupon -> coupon.getDiscountAmount().compareTo(new BigDecimal(0)) > 0 && coupon.getDiscountAmount().compareTo(totalAmount) < 0)
                // 类型转换
                .map(coupon -> BeanUtils.copyBean(coupon, AvailableCouponsResDTO.class))
                //按优惠金额降序排
                .sorted(Comparator.comparing(AvailableCouponsResDTO::getDiscountAmount).reversed())
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO) {
        //判空
        if (ObjectUtils.isNull(couponUseReqDTO.getOrdersId()) ||
                ObjectUtils.isNull(couponUseReqDTO.getTotalAmount()))
        {
            throw new BadRequestException("优惠券核销的订单信息为空");
        }
        //用户id
        Long userId = UserContext.currentUserId();
        //查询优惠券信息
        Coupon coupon = baseMapper.selectById(couponUseReqDTO.getId());
        // 优惠券判空
        if (coupon == null ) {
            throw new BadRequestException("优惠券不存在");
        }
        if ( ObjectUtils.notEqual(coupon.getUserId(),userId)) {
            throw new BadRequestException("只允许核销自己的优惠券");
        }
        //更新优惠券表的状态
        boolean update = lambdaUpdate()
                .eq(Coupon::getId, couponUseReqDTO.getId())
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .gt(Coupon::getValidityTime, DateUtils.now())
                .le(Coupon::getAmountCondition, couponUseReqDTO.getTotalAmount())
                .set(Coupon::getOrdersId, couponUseReqDTO.getOrdersId())
                .set(Coupon::getStatus, CouponStatusEnum.USED.getStatus())
                .set(Coupon::getUseTime, DateUtils.now())
                .update();
        if (!update) {
            throw new DBException("优惠券核销失败");
        }

        //添加核销记录
        CouponWriteOff couponWriteOff = CouponWriteOff.builder()
                .id(IdUtils.getSnowflakeNextId())
                .couponId(couponUseReqDTO.getId())
                .userId(userId)
                .ordersId(couponUseReqDTO.getOrdersId())
                .activityId(coupon.getActivityId())
                .writeOffTime(DateUtils.now())
                .writeOffManName(coupon.getUserName())
                .writeOffManPhone(coupon.getUserPhone())
                .build();
        if(!couponWriteOffService.save(couponWriteOff)){
            throw new DBException("优惠券核销失败");
        }

        // 计算优惠金额
        BigDecimal discountAmount = CouponUtils.calDiscountAmount(coupon, couponUseReqDTO.getTotalAmount());
        CouponUseResDTO couponUseResDTO = new CouponUseResDTO();
        couponUseResDTO.setDiscountAmount(discountAmount);
        return couponUseResDTO;

    }

    @Override
    public void useBack(CouponUseBackReqDTO couponUseBackReqDTO) {
        couponUseBackService.useBack(couponUseBackReqDTO);

    }
    @Resource
    private CurrentUserInfo currentUserInfo;

    @Override
    public PageResult<CouponInfoResDTO> queryForPageOfOperation(CouponOperationPageQueryReqDTO couponOperationPageQueryReqDTO) {
        PageResult<CouponInfoResDTO> pageResult = new PageResult<>();
        // 1.查询优惠券
        List<Coupon> coupons = lambdaQuery()
                .eq(Coupon::getUserId, currentUserInfo.getId())
                .gt(Coupon::getValidityTime, DateUtils.now())
                .list();
        // 2.组装数据
        List<CouponInfoResDTO> collect = coupons.stream()
                .map(coupon -> BeanUtils.copyBean(coupon, CouponInfoResDTO.class))
                .collect(Collectors.toList());
        // 3.分页
        pageResult.setList(collect);
        pageResult.setTotal((long) collect.size());
        return pageResult;

    }

    @Override
    public List<CouponInfoResDTO> queryForList(Object o, Long aLong, int i) {
        if (ObjectUtils.isNull(o)) {
            return List.of();
        }
        if (o instanceof CouponOperationPageQueryReqDTO) {
            CouponOperationPageQueryReqDTO couponOperationPageQueryReqDTO = (CouponOperationPageQueryReqDTO) o;
            return queryForPageOfOperation(couponOperationPageQueryReqDTO).getList();
        }
        return List.of();
    }

}