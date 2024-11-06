package com.jzo2o.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface ICouponService extends IService<Coupon> {


    /**
     * 抢券
     *
     * @param seizeCouponReqDTO
     */
    void seizeCoupon(SeizeCouponReqDTO seizeCouponReqDTO);

    /**
     * 从缓存中获取活动信息
     * @param id
     * @return
     */
    ActivityInfoResDTO getActivityInfoByIdFromCache(Long id);

    /**
     * 获取可用优惠券列表
     * @param totalAmount
     * @return
     */
    List<AvailableCouponsResDTO> getAvailable(BigDecimal totalAmount);

    /**
     * 使用优惠券
     * @param couponUseReqDTO
     */
    CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO);

    void useBack(CouponUseBackReqDTO couponUseBackReqDTO);

    PageResult<CouponInfoResDTO> queryForPageOfOperation(CouponOperationPageQueryReqDTO couponOperationPageQueryReqDTO);

    List<CouponInfoResDTO> queryForList(Object o, Long aLong, int i);
}
