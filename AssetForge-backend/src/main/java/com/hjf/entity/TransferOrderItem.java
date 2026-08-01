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
 * 转移单-资产明细表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("transfer_order_item")
public class TransferOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 转移单ID
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
     * 冗余快照：品牌/型号
     */
    private String brandModel;

    /**
     * 冗余快照：转移时资产现值
     */
    private BigDecimal netValueAtTransfer;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
