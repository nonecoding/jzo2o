package com.jzo2o.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.market.model.domain.CouponUseBack;

/**
 * <p>
 * 优惠券使用回退记录 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-18
 */
public interface ICouponUseBackService extends IService<CouponUseBack> {

    void useBack(CouponUseBackReqDTO couponUseBackReqDTO);
}
