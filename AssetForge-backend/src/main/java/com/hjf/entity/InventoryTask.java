package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 盘点任务表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("inventory_task")
public class InventoryTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 盘点名称
     */
    private String taskName;

    /**
     * 范围类型 ALL/DEPARTMENT/CATEGORY
     */
    private String scopeType;

    /**
     * 范围值 JSON数组
     */
    private String scopeValue;

    /**
     * 纳入盘点的资产状态列表 JSON数组
     */
    private String assetStatusFilter;

    /**
     * 截止日期
     */
    @TableField("deadline")
    private LocalDate deadLine;

    /**
     * 负责人ID
     */
    private Long responsibleUserId;

    /**
     * PENDING/IN_PROGRESS/COMPLETED
     */
    private String status;

    /**
     * 盘点结论
     */
    private String conclusion;
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

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
