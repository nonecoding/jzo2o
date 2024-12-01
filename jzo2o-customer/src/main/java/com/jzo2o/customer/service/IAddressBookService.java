package com.jzo2o.customer.service;

import com.jzo2o.customer.model.domain.AddressBook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;

/**
 * <p>
 * 地址薄 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-11-03
 */
public interface IAddressBookService extends IService<AddressBook> {

    void add(AddressBookUpsertReqDTO addressBookUpsertReqDTO);

    void delete(Integer id);

    void setDefault(Integer id);
}
