package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.01 16:05
 */

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Tag(name = "用户地址簿相关接口", description = "提供用户地址簿相关接口功能")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询地址", description = "提供地址簿根据id查询地址功能")
    public Result<AddressBook> getAddressById(@PathVariable Long id) {
        log.info("根据id查询地址: {}", id);
        AddressBook addressList = addressBookService.getAddressById(id);
        return Result.success(addressList);
    }

    @GetMapping("/default")
    @Operation(summary = "查询默认地址", description = "提供地址簿查询默认地址功能")
    public Result<AddressBook> getDefaultAddress() {
        log.info("查询默认地址");
        AddressBook defaultAddress = addressBookService.getDefaultAddress();
        return Result.success(defaultAddress);
    }

    @GetMapping("/list")
    @Operation(summary = "查询地址列表", description = "提供地址簿查询地址列表功能")
    public Result<List<AddressBook>> getAddressList() {
        log.info("查询地址列表");
        List<AddressBook> addressList = addressBookService.getAddressList();
        return Result.success(addressList);
    }

    @PostMapping
    @Operation(summary = "新增地址", description = "提供地址簿新增地址功能")
    public Result<Integer> addAddress(@RequestBody AddressBook addressBook) {
        log.info("新增地址: {}", addressBook);
        Integer success = addressBookService.addAddress(addressBook);
        return Result.success(success);
    }

    @PutMapping("/default")
    @Operation(summary = "设置默认地址", description = "提供地址簿设置默认地址功能")
    public Result<Integer> setDefaultAddress(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址: {}", addressBook);
        Integer success = addressBookService.setDefaultAddress(addressBook);
        return Result.success(success);
    }

    @PutMapping
    @Operation(summary = "根据id修改地址", description = "提供地址簿根据id修改地址功能")
    public Result<Integer> updateAddress(@RequestBody AddressBook addressBook) {
        log.info("修改地址: {}", addressBook);
        Integer success = addressBookService.updateAddress(addressBook);
        return Result.success(success);
    }

    // 傻逼前端在请求路径后面多加了一个斜杠
    @DeleteMapping("/")
    @Operation(summary = "根据id删除地址", description = "提供地址簿根据id删除地址功能")
    public Result<Integer> deleteAddress(Long id) {
        log.info("根据id删除地址: {}", id);
        Integer success = addressBookService.deleteAddress(id);
        return Result.success(success);
    }
}
