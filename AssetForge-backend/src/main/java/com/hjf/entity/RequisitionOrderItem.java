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
 * 申领单-资产明细表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("requisition_order_item")
public class RequisitionOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申领单ID
     */
    private Long orderId;

    /**
     * 申领时选择的分类ID
     */
    private Long categoryId;

    /**
     * 冗余快照：申领时分类名称
     */
    private String categoryName;

    /**
     * 申领数量
     */
    private Integer quantity;

    /**
     * 冗余快照：出库时写入
     */
    private String assetCode;

    /**
     * 冗余快照：出库时写入
     */
    private String assetName;

    /**
     * 冗余快照：出库时写入
     */
    private String brandModel;

    /**
     * 冗余快照：出库时写入
     */
    private String ledgerNo;

    /**
     * 冗余快照：出库时写入的现值
     */
    private BigDecimal netValueAtApply;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
