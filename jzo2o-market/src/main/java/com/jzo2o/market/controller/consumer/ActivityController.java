package com.jzo2o.market.controller.consumer;

import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.SeizeCouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("consumerActivityController")
@RequestMapping("/consumer/activity")
@Api(tags = "用户端-活动相关接口")
@RequiredArgsConstructor
public class ActivityController {

    @Resource
    private IActivityService activityService;
    private final ICouponService couponService;

    @GetMapping("/list")
    @ApiOperation("用户端抢券列表分页接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tabType", value = "页面tab类型，1：疯抢中，2：即将开始", required = true, dataTypeClass = Integer.class)})
    public List<SeizeCouponInfoResDTO> queryForPage(@RequestParam(value = "tabType",required = true) Integer tabType) {
        return activityService.queryForListFromCache(tabType);
    }


    @ApiOperation("抢券接口")
    @PostMapping("/seize")
    public void seizeCoupon(@RequestBody SeizeCouponReqDTO seizeCouponReqDTO) {
        couponService.seizeCoupon(seizeCouponReqDTO);
    }


}