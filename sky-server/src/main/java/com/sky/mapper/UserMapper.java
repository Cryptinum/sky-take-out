package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.28 00:26
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
