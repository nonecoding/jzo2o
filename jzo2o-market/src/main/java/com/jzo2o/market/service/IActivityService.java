package com.jzo2o.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.SeizeCouponInfoResDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface IActivityService extends IService<Activity> {

    /**
     * 活动预热
     */
    void preHeat();

    /**
     * 用户端抢券列表分页查询活动信息
     *
     * @param tabType 页面类型
     * @return
     */
    List<SeizeCouponInfoResDTO> queryForListFromCache(Integer tabType);


    ActivityInfoResDTO getActivityInfoByIdFromCache(Long id);

    /**
     * 扣减库存
     * @param id 活动id
     *  如果扣减库存失败抛出异常
     */
    public void deductStock(Long id);

    void updateStatus();

}
