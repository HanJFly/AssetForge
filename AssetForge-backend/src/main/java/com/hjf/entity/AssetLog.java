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
 * 资产流转日志表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("asset_log")
public class AssetLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * REGISTER/APPLY/TRANSFER/RETURN/SCRAP/INVENTORY/DEPRECIATION/LOSS
     */
    private String operationType;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 变更前使用人ID
     */
    private Long fromUserId;

    /**
     * 变更后使用人ID
     */
    private Long toUserId;

    /**
     * 变更详情 JSON
     */
    private String detail;

    /**
     * 操作时间
     */
    private LocalDateTime createdAt;
}
