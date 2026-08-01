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
 * 报废单-资产明细表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("scrap_order_item")
public class ScrapOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报废单ID
     */
    private Long orderId;

    /**
     * 资产ID
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
     * 冗余快照：报废时资产原值
     */
    private BigDecimal originalValueAtScrap;

    /**
     * 冗余快照：报废时资产现值
     */
    private BigDecimal netValueAtScrap;

    /**
     * 冗余快照：报废时累计折旧
     */
    private BigDecimal accumulatedDepreciationAtScrap;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
