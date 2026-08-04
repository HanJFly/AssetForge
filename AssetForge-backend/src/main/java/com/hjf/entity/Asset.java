package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 资产表（管理信息）
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
public class Asset implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产条码，唯一索引 AST-YYYYMMDD-NNNNNN
     */
    private String assetCode;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产分类ID
     */
    private Long categoryId;

    /**
     * 品牌/型号
     */
    private String brandModel;

    /**
     * 规格参数
     */
    private String specification;

    /**
     * 用途 OFFICE/PRODUCTION/RD/ADMIN
     */
    private String purpose;

    /**
     * 来源 PURCHASE/LEASE
     */
    private String sourceType;

    /**
     * 购置日期
     */
    private LocalDate purchaseDate;

    /**
     * 购置金额（元）
     */
    private BigDecimal purchaseAmount;

    /**
     * 所属部门ID
     */
    private Long departmentId;

    /**
     * 冗余快照：所属部门名称
     */
    private String departmentName;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 存放地点
     */
    private String location;

    /**
     * 状态 PENDING/STOCK/ASSIGNED/SCRAPPED/LOST
     */
    private String status;

    /**
     * 当前使用人ID，库存时NULL
     */
    private Long currentUserId;

    /**
     * 备注
     */
    private String remark;


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
