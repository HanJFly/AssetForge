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
 * 盘亏单表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("loss_order")
public class LossOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单号 LOSS-YYYYMMDD-NNNNNN
     */
    private String orderNo;

    /**
     * 关联盘点任务ID
     */
    private Long inventoryTaskId;

    /**
     * 关联盘点明细ID
     */
    private Long inventoryDetailId;
    /**
     * 盘亏资产ID
     */
    private Long assetId;

    /**
     * 冗余快照：资产条码
     */
    private String assetCode;

    /**
     * 冗余快照：资产名称
     */
    private String assetName;

    /**
     * 冗余快照：资产分类名称
     */
    private String categoryName;

    /**
     * 冗余快照：台账编号
     */
    private String ledgerNo;

    /**
     * 冗余快照：责任人ID（当前使用人）
     */
    private Long responsibleUserId;

    /**
     * 备注（从盘点明细带过来的盘点备注）
     */
    private String remark;

    /**
     * 冗余快照：责任人姓名
     */
    private String responsibleUserName;

    /**
     * 冗余快照：责任人工号
     */
    private String responsibleUserEmployeeNo;

    /**
     * 冗余快照：责任部门ID
     */
    private Long responsibleDepartmentId;

    /**
     * 冗余快照：责任部门名称
     */
    private String responsibleDepartmentName;

    /**
     * 建议赔偿金额（按资产现值）
     */
    private BigDecimal suggestedCompensation;

    /**
     * 实际赔付金额
     */
    private BigDecimal actualCompensation;

    /**
     * PENDING_COMPENSATION/COMPENSATED/EXEMPTING/EXEMPTED
     */
    private String compensationStatus;

    /**
     * 豁免原因
     */
    private String exemptionReason;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理意见
     */
    private String handlingRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
