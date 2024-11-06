package com.jzo2o.customer.controller.consumer;

import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 地址簿相关接口
 *
 * @author itcast
 * @create 2023/7/7 19:34
 **/
@RestController("AddressController")
@RequestMapping("/consumer/address-book")
@Api(tags = "用户端 - 地址簿相关接口")
public class AddressController {
    @Resource
    private IAddressBookService addressBookService;

    @PostMapping
    @ApiOperation("新增地址簿接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "addressBookUpsertReqDTO", value = "地址簿接口dto", required = true, dataTypeClass = String.class)
    })
    public void add(@RequestBody AddressBookUpsertReqDTO addressBookUpsertReqDTO) {
        addressBookService.add(addressBookUpsertReqDTO);
    }

    @GetMapping("/page")
    @ApiOperation("地址薄分页查询接口")

    public void page() {
        addressBookService.page();
    }


    @GetMapping("/{id}")
    @ApiOperation("地址簿获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "地址簿id", required = true, dataTypeClass = String.class)
    })
    public void getById(@PathVariable Integer id) {
        addressBookService.getById(id);
    }


    @PutMapping("/{id}")
    @ApiOperation("地址簿升级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "地址簿id", required = true, dataTypeClass = String.class)
    })
    public void update(@PathVariable Integer id) {
        addressBookService.update(id);
    }


    @DeleteMapping("/{id}")
    @ApiOperation("地址簿删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "地址簿id", required = true, dataTypeClass = String.class)
    })
    public void delete(@PathVariable Integer id) {
        addressBookService.delete(id);
    }


    @PutMapping("/default")
    @ApiOperation("设置/取消默认地址")

    public void setDefault(@PathVariable Integer id) {
        addressBookService.setDefault(id);
    }

}
