package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 条件查询
     *
     * @param addressBook
     * @return
     */
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    /**
     * 新增地址
     *
     * @param addressBook
     */
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 根据id查询（只能查询当前登录用户自己的地址）
     *
     * @param id
     * @return
     */
    public AddressBook getById(Long id) {
        return checkBelong(id);
    }

    /**
     * 根据id修改地址（只能修改当前登录用户自己的地址）
     *
     * @param addressBook
     */
    public void update(AddressBook addressBook) {
        checkBelong(addressBook.getId());
        addressBookMapper.update(addressBook);
    }

    /**
     * 设置默认地址（只能操作当前登录用户自己的地址）
     *
     * @param addressBook
     */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        //0、校验该地址属于当前登录用户，防止越权把他人地址设为默认
        checkBelong(addressBook.getId());

        //1、将当前用户的所有地址修改为非默认地址 update address_book set is_default = ? where user_id = ?
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        //2、将当前地址改为默认地址 update address_book set is_default = ? where id = ?
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据id删除地址（只能删除当前登录用户自己的地址）
     *
     * @param id
     */
    public void deleteById(Long id) {
        checkBelong(id);
        addressBookMapper.deleteById(id);
    }

    /**
     * 校验地址存在且归属当前登录用户
     * 之前的实现直接按 id 查询/修改/删除，任何登录用户只要猜对 id 就能操作他人地址（越权漏洞）
     *
     * @param id 地址id
     * @return 校验通过的地址实体
     */
    private AddressBook checkBelong(Long id) {
        if (id == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        AddressBook addressBook = addressBookMapper.getById(id);
        //userId 在库中非空，用它做比较可避免未登录时 BaseContext 为 null 造成 NPE
        if (addressBook == null || !addressBook.getUserId().equals(BaseContext.getCurrentId())) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        return addressBook;
    }

}
