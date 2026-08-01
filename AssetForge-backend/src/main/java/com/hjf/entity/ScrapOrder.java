package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 报废单主表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("scrap_order")
public class ScrapOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单号 SCR-YYYYMMDD-NNNNNN
     */
    private String orderNo;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 冗余快照：申请人姓名
     */
    private String applicantName;

    /**
     * 冗余快照：申请人工号
     */
    private String applicantEmployeeNo;

    /**
     * 冗余快照：申请人部门ID
     */
    private Long applicantDepartmentId;

    /**
     * 冗余快照：申请人部门名称
     */
    private String applicantDepartmentName;

    /**
     * 报废原因
     */
    private String reason;

    /**
     * 估计残值
     */
    private BigDecimal estimatedResidualValue;

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
