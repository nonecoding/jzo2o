package com.jzo2o.customer.service.impl;

import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.mapper.AddressBookMapper;
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

}
