package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 审批记录表（统一审批流）
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("approval_record")
public class ApprovalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批类型 REGISTER/APPLY/TRANSFER/RETURN/SCRAP
     */
    private String approvalType;

    /**
     * 目标类型 requisition_order/transfer_order/return_order/scrap_order/asset
     */
    private String targetType;

    /**
     * 目标记录ID
     */
    private Long targetId;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * PENDING/APPROVED/REJECTED
     */
    private String approvalStatus;

    /**
     * 审批意见
     */
    private String approvalRemark;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 转交到的用户ID
     */
    private Long transferredTo;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
