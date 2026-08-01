package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.FileAttachmentBindParam;
import com.hjf.param.FileAttachmentListParam;
import com.hjf.service.IFileAttachmentService;
import com.hjf.vo.FileAttachmentListVO;
import com.hjf.vo.FileAttachmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 文件附件表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/file")
public class FileAttachmentController {

    @Autowired
    private IFileAttachmentService fileAttachmentService;

    /*
    * 上传文件
    * */
    @RequestMapping("/upload")
    public Result<FileAttachmentVO> uploadFile(@RequestParam ("file") MultipartFile  file,
                                               @RequestParam(value = "bizType" , required = false) String bizType) throws IOException {
        return fileAttachmentService.uploadFile(file,bizType);

    }

    /*
    * 附件列表
    * */
    @PostMapping("/list")
    public Result<FileAttachmentListVO> list(@RequestBody FileAttachmentListParam param){
        return fileAttachmentService.list(param);
    }


    /*
    * 绑定业务
    * */
    @PostMapping("/bind")
    public Result<String> bindFile(@RequestBody FileAttachmentBindParam param){
        return fileAttachmentService.bindFile(param);
    }
}
