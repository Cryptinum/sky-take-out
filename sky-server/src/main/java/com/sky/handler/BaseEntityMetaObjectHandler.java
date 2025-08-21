package com.sky.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *
 * @author Zhao Chonghao
 * Create by 2025.08.21 21:16
 */

@Component
@Slf4j
public class BaseEntityMetaObjectHandler implements MetaObjectHandler {

    /**
     * 在执行 insert 操作时，自动填充字段
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("INSERT操作，自动填充create和update字段");
        // setFieldValByName(字段名, 字段值, metaObject)
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createUser", BaseContext::getCurrentId, Long.class);
        this.strictInsertFill(metaObject, "updateUser", BaseContext::getCurrentId, Long.class);
    }

    /**
     * 在执行 update 操作时，自动填充字段
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("UPDATE操作，自动填充update字段");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateUser", BaseContext::getCurrentId, Long.class);
    }
}
