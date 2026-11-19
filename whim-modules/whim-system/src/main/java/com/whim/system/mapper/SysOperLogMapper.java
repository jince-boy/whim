package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 操作日志持久化。
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
    /**
     * 在清空边界内批量逻辑删除并写入删除审计信息。
     * 条件删除无法同时自动填充删除审计字段，逐批载入主键又会增加数据库往返。
     *
     * @param lastId 操作开始时观察到的最大日志主键
     * @param operatorId 执行清空的用户主键
     */
    @Update("""
            UPDATE sys_oper_log
            SET deleted = id, delete_by = #{operatorId}, delete_time = CURRENT_TIMESTAMP(3)
            WHERE deleted = 0 AND id <= #{lastId}
            """)
    void clearOperLogs(@Param("lastId") Long lastId, @Param("operatorId") Long operatorId);
}
