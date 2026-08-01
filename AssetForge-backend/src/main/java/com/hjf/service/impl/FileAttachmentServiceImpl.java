package com.hjf.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hjf.common.result.Result;
import com.hjf.entity.FileAttachment;
import com.hjf.mapper.FileAttachmentMapper;
import com.hjf.param.FileAttachmentBindParam;
import com.hjf.param.FileAttachmentListParam;
import com.hjf.service.IFileAttachmentService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.FileAttachmentListVO;
import com.hjf.vo.FileAttachmentListVOChildren;
import com.hjf.vo.FileAttachmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 文件附件表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class FileAttachmentServiceImpl extends ServiceImpl<FileAttachmentMapper, FileAttachment> implements IFileAttachmentService {

    @Autowired
    private FileAttachmentMapper fileAttachmentMapper;
    @Override
    public Result<FileAttachmentVO> uploadFile(MultipartFile file, String bizType) throws IOException {
         final String BASE_PATH = "D:/Mystudy/AssetForge/AssetForge-backend/src/main/resources/image/";
         if(!file.isEmpty()){
             //获取文件原始名
             String originalFilename = file.getOriginalFilename();
             //截取文件后缀
             String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
             //用UUID生成一个 128 位的随机标识符，并移除连字符，拼接上后缀名
             String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extName;
             //拼接完整的文件路径
             File targetFile = new File(BASE_PATH + uniqueFileName);

             //如果目标不存在，则创建它
             if(!targetFile.getParentFile().exists()){
                 targetFile.getParentFile().mkdirs();
             }
             //保存文件
             file.transferTo(targetFile);

             //插入数据库
             FileAttachment fileAttachment = new FileAttachment();
             fileAttachment.setFileUrl(uniqueFileName);
             fileAttachment.setFileName(originalFilename);
             fileAttachment.setFileSize(file.getSize());
             fileAttachment.setContentType(file.getContentType());
             fileAttachment.setBizType(bizType);
             fileAttachment.setUploadedBy(1L);
             fileAttachmentMapper.insert(fileAttachment);

             FileAttachmentVO fileAttachmentVO = new FileAttachmentVO();
             fileAttachmentVO.setId(fileAttachment.getId());
             fileAttachmentVO.setFileUrl(uniqueFileName);
             fileAttachmentVO.setFileName(originalFilename);
             fileAttachmentVO.setFileSize(file.getSize());
             fileAttachmentVO.setContentType(file.getContentType());
             fileAttachmentVO.setBizType(bizType);

             return Result.ok(fileAttachmentVO);
         }
         return Result.fail("文件不能为空");
    }

    /*
    * 绑定文件
    * */
    @Override
    public Result<String> bindFile(FileAttachmentBindParam param) {
        FileAttachment fileAttachment = fileAttachmentMapper.selectById(param.getAttachmentIds());
        if (fileAttachment == null){
            return Result.fail("未找到附件");
        }
        fileAttachment.setBizId(param.getBizId());
        fileAttachment.setBizType(param.getBizType());
        fileAttachmentMapper.updateById(fileAttachment);
        return Result.ok("绑定成功");
    }

    @Override
    public Result<FileAttachmentListVO> list(FileAttachmentListParam param) {
        LambdaQueryWrapper<FileAttachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileAttachment::getBizType, param.getBizType());
        queryWrapper.eq(FileAttachment::getBizId, param.getBizId());
        List<FileAttachment> fileAttachments = fileAttachmentMapper.selectList(queryWrapper);
        if(fileAttachments == null){
            return Result.fail("未找到附件");
        }
        FileAttachmentListVO fileAttachmentListVO = new FileAttachmentListVO();
        for (FileAttachment fileAttachment : fileAttachments) {
            FileAttachmentListVOChildren fileAttachmentListVOChildren  = new FileAttachmentListVOChildren();
            fileAttachmentListVOChildren.setId(fileAttachment.getId());
            fileAttachmentListVOChildren.setFileUrl(fileAttachment.getFileUrl());
            fileAttachmentListVOChildren.setFileName(fileAttachment.getFileName());
            fileAttachmentListVOChildren.setFileSize(fileAttachment.getFileSize());
            fileAttachmentListVOChildren.setContentType(fileAttachment.getContentType());
            fileAttachmentListVO.getChildren().add(fileAttachmentListVOChildren);
        }
        return Result.ok(fileAttachmentListVO);

    }
}
