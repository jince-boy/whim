package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysTenant extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 508226982539049301L;

    /**
     * id
     */
    private Long id;

    /**
     * 租户管理员ID
     */
    private Long userId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactNumber;

    /**
     * 企业地址
     */
    private String companyAddress;

    /**
     * 企业统一社会信用代码
     */
    private String companyCode;

    /**
     * 套餐ID
     */
    private Long packageId;

    /**
     * 租户状态(0正常 1停用)
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 用户限额(0不限制)
     */
    private Integer accountCount;

    /**
     * 绑定域名
     */
    private String domain;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志(0-未删除 1-已删除)
     */
    @TableLogic
    private Integer deleted;

}

