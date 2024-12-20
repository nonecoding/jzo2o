package com.jzo2o.foundations.controller.consumer;

import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.ServeAggregationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
@Validated
@RestController("consumerServeController")
@RequestMapping("/customer/serve")
@Api(tags = "用户端 - 首页服务查询接口")
public class FirstPageServeController {
    @Resource
    private HomeService homeService;
    @Resource
    private ServeAggregationService serveAggregationService;

    @GetMapping("/firstPageServeList")
    @ApiOperation("首页服务列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "regionId", value = "区域id", required = true, dataTypeClass = Long.class)
    })
    public List<ServeCategoryResDTO> serveCategory(@RequestParam("regionId") Long regionId) {
        List<ServeCategoryResDTO> serveCategoryResDTOS = homeService.queryServeIconCategoryByRegionIdCache(regionId);
        return serveCategoryResDTOS;
    }


    @GetMapping("/search")
    @ApiOperation("首页服务搜索")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cityCode", value = "城市编码", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "serveTypeId", value = "服务类型id", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "keyword", value = "关键词", dataTypeClass = String.class)
    })
    public List<ServeSimpleResDTO> findServeList(@RequestParam("cityCode") String cityCode,
                                                 @RequestParam(value = "serveTypeId", required = false) Long serveTypeId,
                                                 @RequestParam(value = "keyword", required = false) String keyword) {
        List<ServeSimpleResDTO> serveList = serveAggregationService.findServeList(cityCode, serveTypeId, keyword);
        return serveList;
    }
}