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
 * 资产台账快照表（月度备份）
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("asset_ledger_snapshot")
public class AssetLedgerSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联 asset_ledger.id
     */
    private Long ledgerId;

    /**
     * 关联 asset.id
     */
    private Long assetId;

    /**
     * 快照月份 YYYY-MM
     */
    private String snapshotMonth;

    /**
     * 快照时复制
     */
    private String assetCode;

    /**
     * 快照时复制
     */
    private String assetName;

    /**
     * 快照时复制
     */
    private String categoryName;

    /**
     * 快照时复制：所属部门
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
     * 标准使用年限
     */
    private Integer standardLifeMonths;

    /**
     * 月折旧额
     */
    private BigDecimal monthlyDepreciation;

    /**
     * 累计折旧（折旧计算后的值）
     */
    private BigDecimal accumulatedDepreciation;

    /**
     * 现值
     */
    private BigDecimal netValue;

    /**
     * 已使用月数
     */
    private Integer monthsUsed;

    /**
     * 当前使用人姓名
     */
    private String currentUserName;

    /**
     * 当前使用人工号
     */
    private String currentUserEmployeeNo;

    /**
     * 当前使用人部门
     */
    private String currentUserDepartment;

    /**
     * 资产状态 STOCK/ASSIGNED/SCRAPPED/LOST
     */
    private String assetStatus;

    /**
     * 快照创建时间
     */
    private LocalDateTime createdAt;
}
