package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.01 16:04
 */

@Service
public class AddressBookServiceImpl
        extends ServiceImpl<AddressBookMapper, AddressBook>
        implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 根据id查询地址
     *
     * @param id
     * @return
     */
    @Override
    public AddressBook getAddressById(Long id) {
        return addressBookMapper.selectById(id);
    }

    /**
     * 查询默认地址
     *
     * @return
     */
    @Override
    public AddressBook getDefaultAddress() {
        return addressBookMapper.selectOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1));
    }

    /**
     * 查询地址列表
     *
     * @return
     */
    @Override
    public List<AddressBook> getAddressList() {
        return addressBookMapper.selectList(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId()));
    }

    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public Integer addAddress(AddressBook addressBook) {
        // 判断是否已经存在默认地址，如果存在那么新增的地址不作为默认地址
        AddressBook defaultAddress = this.getDefaultAddress();
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(defaultAddress == null ? 1 : 0);
        return addressBookMapper.insert(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @Override
    @Transactional
    public Integer setDefaultAddress(AddressBook addressBook) {
        AddressBook defaultAddress = this.getDefaultAddress();

        // 如果设置的默认地址已经是默认地址了，那么直接返回1
        if (Objects.equals(defaultAddress.getId(), addressBook.getId())) {
            return 1;
        }

        // 交换默认地址
        defaultAddress.setIsDefault(0);
        addressBook.setIsDefault(1);
        int i = addressBookMapper.updateById(defaultAddress);
        int j = addressBookMapper.updateById(addressBook);
        return i + j;
    }

    /**
     * 修改地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public Integer updateAddress(AddressBook addressBook) {
        return addressBookMapper.updateById(addressBook);
    }

    /**
     * 删除地址
     *
     * @param id
     * @return
     */
    @Override
    public Integer deleteAddress(Long id) {
        return addressBookMapper.deleteById(id);
    }
}
