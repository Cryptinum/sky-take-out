package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.AddressBook;

import java.util.List;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.01 16:04
 */

public interface AddressBookService extends IService<AddressBook> {
    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    Integer addAddress(AddressBook addressBook);

    /**
     * 查询地址列表
     * @return
     */
    List<AddressBook> getAddressList();

    /**
     * 查询默认地址
     *
     * @return
     */
    AddressBook getDefaultAddress();

    /**
     * 根据id查询地址
     *
     * @param id
     * @return
     */
    AddressBook getAddressById(Long id);

    /**
     * 设置默认地址
     *
     * @param addressBook@return
     */
    Integer setDefaultAddress(AddressBook addressBook);

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    Integer updateAddress(AddressBook addressBook);

    /**
     * 删除地址
     * @param id
     * @return
     */
    Integer deleteAddress(Long id);
}
