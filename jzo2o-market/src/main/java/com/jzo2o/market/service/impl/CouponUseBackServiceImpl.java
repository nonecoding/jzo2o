package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.market.mapper.CouponUseBackMapper;
import com.jzo2o.market.model.domain.CouponUseBack;
import com.jzo2o.market.service.ICouponUseBackService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 优惠券使用回退记录 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-18
 */
@Service
public class CouponUseBackServiceImpl extends ServiceImpl<CouponUseBackMapper, CouponUseBack> implements ICouponUseBackService {

    @Override
    public void useBack(CouponUseBackReqDTO couponUseBackReqDTO) {

    }
}
