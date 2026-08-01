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
 * 归还单主表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("return_order")
public class ReturnOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单号 RET-YYYYMMDD-NNNNNN
     */
    private String orderNo;

    /**
     * 归还人ID
     */
    private Long returnUserId;

    /**
     * 冗余快照：归还人姓名
     */
    private String returnUserName;

    /**
     * 冗余快照：归还人工号
     */
    private String returnUserEmployeeNo;

    /**
     * 冗余快照：归还人部门ID
     */
    private Long returnUserDepartmentId;

    /**
     * 冗余快照：归还人部门名称
     */
    private String returnUserDepartmentName;

    /**
     * 归还原因
     */
    private String reason;

    /**
     * 预计归还日期
     */
    private LocalDate expectedReturnDate;

    /**
     * 冗余快照：接收仓管员姓名
     */
    private String receiverName;

    /**
     * 入库确认时间
     */
    private LocalDateTime receivedAt;

    /**
     * 入库确认备注
     */
    private String confirmRemark;
    /**
     * 订单状态 PENDING/COMPLETED
     */
    private String orderStatus;

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
