package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 部门表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 上级部门ID，顶级为NULL
     */
    private Long parentId;

    /**
     * 部门管理员用户ID
     */
    private Long managerUserId;

    /**
     * 同级内排序
     */
    private Integer sortOrder;

    /**
     * 软删除标记 0-正常 1-已删除
     */
    private Byte isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
