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
 * 文件附件表
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Getter
@Setter
@ToString
@TableName("file_attachment")
public class FileAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型 SCRAP_PHOTO 等
     */
    private String bizType;

    /**
     * 关联业务记录ID
     */
    private Long bizId;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 上传人ID
     */
    private Long uploadedBy;

    /**
     * 上传时间
     */
    private LocalDateTime createdAt;
}
