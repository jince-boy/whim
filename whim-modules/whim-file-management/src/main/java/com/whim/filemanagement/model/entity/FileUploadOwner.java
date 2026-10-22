package com.whim.filemanagement.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 同一用户创建任务时的额度锁，使用主键行锁防止并发绕过数量和容量限制。
 */
@Data
public class FileUploadOwner {
    @TableId(type = IdType.INPUT)
    private Long ownerId;
}

