package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 资产台账表（财务信息）
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("asset_ledger")
public class AssetLedger implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 台账编号 LEDGER-YYYYMMDD-NNNNNN
     */
    private String ledgerNo;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 所属部门ID
     */
    private Long departmentId;

    /**
     * 冗余快照：所属部门名称
     */
    private String departmentName;

    /**
     * 入账日期
     */
    private LocalDate entryDate;

    /**
     * 原值
     */
    private BigDecimal originalValue;

    /**
     * 残值率
     */
    private BigDecimal residualRate;

    /**
     * 标准使用年限（月）
     */
    private Integer standardLifeMonths;

    /**
     * 月折旧额
     */
    private BigDecimal monthlyDepreciation;

    /**
     * 累计折旧
     */
    private BigDecimal accumulatedDepreciation;

    /**
     * 现值 = 原值 - 累计折旧
     */
    private BigDecimal netValue;

    /**
     * 已使用月数
     */
    private Integer monthsUsed;

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
