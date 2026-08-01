package com.hjf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 资产分类表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("asset_category")
public class AssetCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 上级分类ID，一级为NULL
     */
    private Long parentId;

    /**
     * 标准使用年限（月），一级分类为NULL
     */
    private Integer standardLifeMonths;

    /**
     * 同级内排序
     */
    private Integer sortOrder;

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
