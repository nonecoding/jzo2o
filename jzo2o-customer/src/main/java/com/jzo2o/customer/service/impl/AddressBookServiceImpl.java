package com.jzo2o.customer.service.impl;

import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.mapper.AddressBookMapper;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 地址薄 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2024-11-03
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {

    @Override
    public void add(AddressBookUpsertReqDTO addressBookUpsertReqDTO) {

        AddressBook addressBook = AddressBook.builder()
                .name(addressBookUpsertReqDTO.getName())
                .phone(addressBookUpsertReqDTO.getPhone())
                .province(addressBookUpsertReqDTO.getProvince())
                .city(addressBookUpsertReqDTO.getCity())
                .county(addressBookUpsertReqDTO.getCounty())
                .address(addressBookUpsertReqDTO.getAddress())
                .isDefault(addressBookUpsertReqDTO.getIsDefault())
                .build();
        save(addressBook);
    }

    @Override
    public void delete(Integer id) {

        removeById(id);
    }

    @Override
    public void setDefault(Integer id) {

        AddressBook addressBook = getById(id);
        addressBook.setIsDefault(1);
        updateById(addressBook);
    }
}
