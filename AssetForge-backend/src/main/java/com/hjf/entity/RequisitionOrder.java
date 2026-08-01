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
 * 申领单主表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("requisition_order")
public class RequisitionOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单号 REQ-YYYYMMDD-NNNNNN
     */
    private String orderNo;

    /**
     * 申领人ID
     */
    private Long applicantId;

    /**
     * 冗余快照：申领人姓名
     */
    private String applicantName;

    /**
     * 冗余快照：申领人工号
     */
    private String applicantEmployeeNo;

    /**
     * 冗余快照：申领部门ID
     */
    private Long applicantDepartmentId;

    /**
     * 冗余快照：申领部门名称
     */
    private String applicantDepartmentName;

    /**
     * 申领原因
     */
    private String reason;

    /**
     * 期望日期
     */
    private LocalDate expectedDate;

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
     * 冗余快照：出库仓管员姓名
     */
    private String outboundStaffName;

    /**
     * 出库时间
     */
    private LocalDateTime outboundAt;

    /**
     * 出库确认备注
     */
    private String confirmRemark;

    /**
     * 订单状态 PENDING/APPROVED/OUTBOUND/EXPIRED
     */
    private String orderStatus;

    /**
     * 超时失效时间
     */
    private LocalDateTime expiredAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
