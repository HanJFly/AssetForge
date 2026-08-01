package com.hjf.service;

import com.hjf.common.result.Result;
import com.hjf.entity.FileAttachment;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.FileAttachmentBindParam;
import com.hjf.param.FileAttachmentListParam;
import com.hjf.vo.FileAttachmentListVO;
import com.hjf.vo.FileAttachmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 文件附件表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IFileAttachmentService extends IService<FileAttachment> {

    Result<FileAttachmentVO> uploadFile(MultipartFile file, String bizType) throws IOException;

    Result<String> bindFile(FileAttachmentBindParam param);

    Result<FileAttachmentListVO> list(FileAttachmentListParam param);
}
