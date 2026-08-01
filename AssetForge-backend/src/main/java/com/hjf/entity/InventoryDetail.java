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
 * 盘点明细表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("inventory_detail")
public class InventoryDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 盘点任务ID
     */
    private Long taskId;

    /**
     * 资产ID（盘盈时为NULL）
     */
    private Long assetId;

    /**
     * LOSS/GAIN/MISMATCH/NORMAL
     */
    private String result;

    /**
     * 盘盈：实物资产名称
     */
    private String foundAssetName;

    /**
     * 盘盈：实物资产分类
     */
    private String foundAssetCategory;

    /**
     * 盘盈：实物存放地点
     */
    private String foundAssetLocation;

    /**
     * 盘盈：实物上贴附的条码
     */
    private String foundAssetCode;

    /**
     * 系统中记录的使用人（盘盈时NULL）
     */
    private Long systemUserId;

    /**
     * 实际使用人（盘亏时NULL）
     */
    private Long actualUserId;

    /**
     * 实际存放地点（盘亏时NULL）
     */
    private String actualLocation;

    /**
     * 盘点人ID
     */
    private Long checkerId;

    /**
     * 盘点时间
     */
    private LocalDateTime checkedAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
