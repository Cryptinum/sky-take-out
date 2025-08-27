package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.27 23:57
 */

public interface UserService extends IService<User> {

    /**
     * 小程序端用户登录
     * @param userLoginDTO
     * @return
     */
    UserLoginVO wxLogin(UserLoginDTO userLoginDTO);
}
