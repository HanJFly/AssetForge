package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@Table(name = "`user`")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，唯一
     */
    private String username;

    /**
     * bcrypt加密密码
     */
    private String passwordHash;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 所属部门ID（仓管员为空）
     */
    private Long departmentId;

    /**
     * 冗余快照：所属部门名称
     */
    private String departmentName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 软删除标记 0-正常 1-已删除
     */
    private Byte isDeleted;

    /**
     * 状态 ACTIVE/DISABLED
     */
    private String status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
