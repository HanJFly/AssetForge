package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 转移单主表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("transfer_order")
public class TransferOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单号 TRF-YYYYMMDD-NNNNNN
     */
    private String orderNo;

    /**
     * 转出人ID
     */
    private Long fromUserId;

    /**
     * 冗余快照：转出人姓名
     */
    private String fromUserName;

    /**
     * 冗余快照：转出人工号
     */
    private String fromUserEmployeeNo;

    /**
     * 冗余快照：转出部门ID
     */
    private Long fromUserDepartmentId;

    /**
     * 冗余快照：转出部门名称
     */
    private String fromUserDepartmentName;

    /**
     * 转入人ID
     */
    private Long toUserId;

    /**
     * 冗余快照：转入人姓名
     */
    private String toUserName;

    /**
     * 冗余快照：转入人工号
     */
    private String toUserEmployeeNo;

    /**
     * 冗余快照：转入部门ID
     */
    private Long toUserDepartmentId;

    /**
     * 冗余快照：转入部门名称
     */
    private String toUserDepartmentName;

    /**
     * 转移原因
     */
    private String reason;

    /**
     * 转移日期
     */
    private LocalDate transferDate;

    /**
     * PENDING/APPROVED/REJECTED
     */
    private String approvalStatus;

    /**
     * 审批意见
     */
    private String approvalRemark;

    /**
     * 冗余快照：审批人姓名
     */
    private String approverName;

    /**
     * 审批通过时间
     */
    private LocalDateTime approvedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
