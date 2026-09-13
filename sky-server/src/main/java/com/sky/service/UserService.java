package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {

    /**
     * 微信登录
     *
     * @param userLoginDTO 携带微信授权码
     * @return 用户信息
     */
    User wxLogin(UserLoginDTO userLoginDTO);

}
