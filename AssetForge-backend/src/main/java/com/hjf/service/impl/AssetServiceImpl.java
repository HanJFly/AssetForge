package com.hjf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.ApprovalRecord;
import com.hjf.entity.Asset;
import com.hjf.entity.AssetCategory;
import com.hjf.entity.Department;
import com.hjf.entity.FileAttachment;
import com.hjf.entity.InventoryDetail;
import com.hjf.entity.LossOrder;
import com.hjf.entity.RequisitionOrderItem;
import com.hjf.entity.ReturnOrderItem;
import com.hjf.entity.ScrapOrderItem;
import com.hjf.entity.TransferOrderItem;
import com.hjf.entity.User;
import com.hjf.mapper.ApprovalRecordMapper;
import com.hjf.mapper.AssetCategoryMapper;
import com.hjf.mapper.AssetMapper;
import com.hjf.mapper.DepartmentMapper;
import com.hjf.mapper.FileAttachmentMapper;
import com.hjf.mapper.InventoryDetailMapper;
import com.hjf.mapper.LossOrderMapper;
import com.hjf.mapper.RequisitionOrderItemMapper;
import com.hjf.mapper.ReturnOrderItemMapper;
import com.hjf.mapper.ScrapOrderItemMapper;
import com.hjf.mapper.TransferOrderItemMapper;
import com.hjf.mapper.UserMapper;
import com.hjf.param.AssetCreateWithFilesParam;
import com.hjf.param.AssetPageParam;
import com.hjf.param.AssetParam;
import com.hjf.param.FileAttachmentBindParam;
import com.hjf.param.LoginUserContext;
import com.hjf.service.IAssetService;
import com.hjf.service.IFileAttachmentService;
import com.hjf.vo.AssetBarcodeDetailVO;
import com.hjf.vo.AssetCreateVO;
import com.hjf.vo.AssetPageVO;
import com.hjf.vo.AssetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 资产表（管理信息）服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class AssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements IAssetService {

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileAttachmentMapper fileAttachmentMapper;

    @Autowired
    private IFileAttachmentService fileAttachmentService;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private InventoryDetailMapper inventoryDetailMapper;

    @Autowired
    private LossOrderMapper lossOrderMapper;

    @Autowired
    private RequisitionOrderItemMapper requisitionOrderItemMapper;

    @Autowired
    private ReturnOrderItemMapper returnOrderItemMapper;

    @Autowired
    private ScrapOrderItemMapper scrapOrderItemMapper;

    @Autowired
    private TransferOrderItemMapper transferOrderItemMapper;

    @Autowired
    private AssetCategoryMapper assetCategoryMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    /*
    * 分页查询
    * */
    @Override
    public AssetPageVO queryPage(AssetPageParam param) {
        // 分页
        PageHelper.startPage(param.getPage(), param.getSize());
        // 查询
        List<AssetVO> assetPageVOList = assetMapper.queryPage(param);

        Page<AssetVO> pageInfo = (Page<AssetVO>) assetPageVOList;
        AssetPageVO assetPageVO = new AssetPageVO();
        assetPageVO.setPage(pageInfo.getPageNum());
        assetPageVO.setSize(pageInfo.getPageSize());
        assetPageVO.setTotal(pageInfo.getTotal());
        assetPageVO.setRecords(pageInfo);

        return assetPageVO;
    }

    /*
    * 资产详情
    * */
    @Override
    public AssetVO detail(AssetParam param) {
        Asset asset = assetMapper.selectById(param.getId());
        if (asset == null) {
            throw new CommonException(404, "资产不存在");
        }

        AssetVO assetVO = BeanUtil.copyProperties(asset, AssetVO.class);

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("id", asset.getCurrentUserId());
        User user = userMapper.selectOne(qw);
        if (user != null) {
            assetVO.setCurrentUserName(user.getRealName());
        }

        List<FileAttachment> fileAttachmentList = fileAttachmentMapper.selectList(
                new QueryWrapper<FileAttachment>()
                        .eq("biz_type", "ASSET")
                        .eq("biz_id", asset.getId())
        );
        if (fileAttachmentList == null) {
            throw new CommonException(404, "未找到附件");
        } else {
            for (FileAttachment fileAttachment : fileAttachmentList) {
                if (fileAttachment.getBizId() != null && fileAttachment.getBizId().equals(asset.getId())) {
                    assetVO.getAttachmentList().add(fileAttachment);
                }
            }
        }
        return assetVO;
    }

    // 登记资产
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetCreateVO create(AssetParam param) {
        if (param.getName() == null) {
            throw new CommonException(400, "资产名称不能为空");
        }
        if (param.getCategoryId() == null) {
            throw new CommonException(400, "资产分类不能为空");
        }
        if (param.getDepartmentId() == null) {
            throw new CommonException(400, "部门不能为空");
        }
        if (param.getBrandModel() == null) {
            throw new CommonException(400, "资产品牌型号不能为空");
        }
        if (param.getSourceType() == null) {
            throw new CommonException(400, "资产来源不能为空");
        }
        if (param.getPurpose() == null) {
            throw new CommonException(400, "资产用途不能为空");
        }
        if (param.getPurchaseAmount() == null) {
            throw new CommonException(400, "资产金额不能为空");
        }
        if (param.getPurchaseDate() == null) {
            throw new CommonException(400, "资产购买时间不能为空");
        }
        if (param.getRemark() == null) {
            throw new CommonException(400, "资产备注不能为空");
        }
        if (param.getAttachmentIds() == null) {
            throw new CommonException(400, "资产附件不能为空");
        }

        Asset asset = BeanUtil.copyProperties(param, Asset.class);

        // 生成资产编号：AST-YYYYMMDD-NNNNNN
        asset.setAssetCode(generateOrderNo());

        Department department = departmentMapper.selectById(param.getDepartmentId());
        asset.setDepartmentName(department.getName());
        asset.setStatus("PENDING");

        assetMapper.insert(asset);

        // 绑定附件
        param.getAttachmentIds().forEach(attachmentId -> {
            FileAttachmentBindParam fileAttachmentBindParam = new FileAttachmentBindParam();
            fileAttachmentBindParam.setAttachmentIds(attachmentId);
            fileAttachmentBindParam.setBizId(asset.getId());
            fileAttachmentBindParam.setBizType("ASSET");
            fileAttachmentService.bindFile(fileAttachmentBindParam);
        });

        LoginUserContext context = LoginUserInfoUtile.get();
        // 生成审批记录
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setApprovalType("ASSET");
        approvalRecord.setTargetType("asset");
        approvalRecord.setTargetId(asset.getId());
        approvalRecord.setApplicantId(context.getId());
        approvalRecord.setApprovalStatus("PENDING");
        approvalRecord.setCreatedAt(LocalDateTime.now());
        approvalRecord.setUpdatedAt(LocalDateTime.now());
        approvalRecordMapper.insert(approvalRecord);

        AssetCreateVO assetCreateVO = new AssetCreateVO();
        assetCreateVO.setId(asset.getId());
        assetCreateVO.setAssetCode(asset.getAssetCode());
        return assetCreateVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetCreateVO createWithFiles(AssetCreateWithFilesParam param, MultipartFile[] files) {
        validateCreateWithFilesParam(param, files);

        List<File> savedFiles = new ArrayList<>();

        try {
            List<FileAttachment> attachments = saveUploadedFiles(files, savedFiles);

            Asset asset = BeanUtil.copyProperties(param, Asset.class);
            asset.setAssetCode(generateOrderNo());

            Department department = departmentMapper.selectById(param.getDepartmentId());
            if (department == null) {
                throw new CommonException(400, "部门不存在");
            }

            asset.setDepartmentName(department.getName());
            asset.setStatus("PENDING");
            assetMapper.insert(asset);

            for (FileAttachment attachment : attachments) {
                attachment.setBizId(asset.getId());
                attachment.setBizType("ASSET");
                fileAttachmentMapper.insert(attachment);
            }

            createApprovalRecord(asset.getId());

            AssetCreateVO assetCreateVO = new AssetCreateVO();
            assetCreateVO.setId(asset.getId());
            assetCreateVO.setAssetCode(asset.getAssetCode());
            return assetCreateVO;
        } catch (Exception e) {
            deleteSavedFiles(savedFiles);
            throw e;
        }
    }

    /*
    * 修改资产
    * */
    @Override
    public void updateAsset(AssetParam param) {
        Asset asset = assetMapper.selectById(param.getId());
        if (asset == null) {
            throw new CommonException(404, "未找到资产");
        }
        if (param.getName() == null) {
            throw new CommonException(400, "资产名称不能为空");
        }
        if (param.getCategoryId() == null) {
            throw new CommonException(400, "资产分类不能为空");
        }
        if (param.getDepartmentId() == null) {
            throw new CommonException(400, "部门不能为空");
        }
        if (param.getBrandModel() == null) {
            throw new CommonException(400, "资产品牌型号不能为空");
        }
        if (param.getSourceType() == null) {
            throw new CommonException(400, "资产来源不能为空");
        }
        if (param.getPurpose() == null) {
            throw new CommonException(400, "资产用途不能为空");
        }
        if (param.getPurchaseAmount() == null) {
            throw new CommonException(400, "资产金额不能为空");
        }
        if (param.getPurchaseDate() == null) {
            throw new CommonException(400, "资产购买时间不能为空");
        }
        if (param.getRemark() == null) {
            throw new CommonException(400, "资产备注不能为空");
        }
        if (param.getAttachmentIds() == null) {
            throw new CommonException(400, "资产附件不能为空");
        }
        asset = BeanUtil.copyProperties(param, Asset.class);
        assetMapper.updateById(asset);

        // 绑定附件
        param.getAttachmentIds().forEach(attachmentId -> {
            FileAttachmentBindParam fileAttachmentBindParam = new FileAttachmentBindParam();
            fileAttachmentBindParam.setAttachmentIds(attachmentId);
            fileAttachmentBindParam.setBizId(param.getId());
            fileAttachmentBindParam.setBizType("ASSET");
            fileAttachmentService.bindFile(fileAttachmentBindParam);
        });
    }

    /*
    * 删除资产
    * */
    @Override
    public void deleteAsset(AssetParam param) {
        Asset asset = assetMapper.selectById(param.getId());
        if (asset == null) {
            throw new CommonException(404, "未找到资产");
        }
        QueryWrapper<InventoryDetail> qwI = new QueryWrapper<InventoryDetail>();
        qwI.eq("asset_id", param.getId());
        Long l = inventoryDetailMapper.selectCount(qwI);
        if (l > 0) {
            throw new CommonException(400, "资产在盘点明细中引用，不能删除");
        }

        QueryWrapper<LossOrder> qwL = new QueryWrapper<LossOrder>();
        qwL.eq("asset_id", param.getId());
        l = lossOrderMapper.selectCount(qwL);
        if (l > 0) {
            throw new CommonException(400, "资产在盘亏单中引用，不能删除");
        }

        QueryWrapper<RequisitionOrderItem> qwR = new QueryWrapper<RequisitionOrderItem>();
        qwR.eq("asset_code", param.getAssetCode());
        l = requisitionOrderItemMapper.selectCount(qwR);
        if (l > 0) {
            throw new CommonException(400, "资产在申领单中引用，不能删除");
        }

        QueryWrapper<ReturnOrderItem> qwRe = new QueryWrapper<ReturnOrderItem>();
        qwRe.eq("asset_code", param.getAssetCode());
        l = returnOrderItemMapper.selectCount(qwRe);
        if (l > 0) {
            throw new CommonException(400, "资产在归还单中引用，不能删除");
        }

        QueryWrapper<ScrapOrderItem> qwS = new QueryWrapper<ScrapOrderItem>();
        qwS.eq("asset_code", param.getAssetCode());
        l = scrapOrderItemMapper.selectCount(qwS);
        if (l > 0) {
            throw new CommonException(400, "资产在报废单中引用，不能删除");
        }

        QueryWrapper<TransferOrderItem> qwT = new QueryWrapper<TransferOrderItem>();
        qwT.eq("asset_code", param.getAssetCode());
        l = transferOrderItemMapper.selectCount(qwT);
        if (l > 0) {
            throw new CommonException(400, "资产在转移单中引用，不能删除");
        }

        asset.setIsDeleted((byte) 1);
        assetMapper.updateById(asset);
    }

    @Override
    public AssetBarcodeDetailVO barcodeDetail(AssetParam param) {
        Asset asset = assetMapper.selectById(param.getId());
        AssetBarcodeDetailVO assetBarcodeDetailVO = BeanUtil.copyProperties(asset, AssetBarcodeDetailVO.class);
        QueryWrapper<AssetCategory> qw = new QueryWrapper<AssetCategory>();
        qw.eq("id", asset.getCategoryId());
        assetBarcodeDetailVO.setCategoryName(assetCategoryMapper.selectOne(qw).getName());
        assetBarcodeDetailVO.setBarcodeValue(asset.getAssetCode());

        if (asset.getCurrentUserId() != null) {
            QueryWrapper<User> qwU = new QueryWrapper<User>();
            qwU.eq("id", asset.getCurrentUserId());
            assetBarcodeDetailVO.setCurrentUserName(userMapper.selectOne(qwU).getRealName());
        }
        return assetBarcodeDetailVO;
    }

    private void validateCreateWithFilesParam(AssetCreateWithFilesParam param, MultipartFile[] files) {
        if (param.getName() == null) {
            throw new CommonException(400, "资产名称不能为空");
        }
        if (param.getCategoryId() == null) {
            throw new CommonException(400, "资产分类不能为空");
        }
        if (param.getDepartmentId() == null) {
            throw new CommonException(400, "部门不能为空");
        }
        if (param.getBrandModel() == null) {
            throw new CommonException(400, "资产品牌型号不能为空");
        }
        if (param.getSourceType() == null) {
            throw new CommonException(400, "资产来源不能为空");
        }
        if (param.getPurpose() == null) {
            throw new CommonException(400, "资产用途不能为空");
        }
        if (param.getPurchaseAmount() == null) {
            throw new CommonException(400, "资产金额不能为空");
        }
        if (param.getPurchaseDate() == null) {
            throw new CommonException(400, "资产购买时间不能为空");
        }
        if (param.getRemark() == null) {
            throw new CommonException(400, "资产备注不能为空");
        }
        if (files == null || files.length == 0) {
            throw new CommonException(400, "资产附件不能为空");
        }
    }

    private List<FileAttachment> saveUploadedFiles(MultipartFile[] files, List<File> savedFiles) {
        final String BASE_PATH = "D:/Mystudy/AssetForge/AssetForge-backend/src/main/resources/image/";
        List<FileAttachment> attachments = new ArrayList<>();
        LoginUserContext context = LoginUserInfoUtile.get();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new CommonException(400, "文件名不合法");
            }

            String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extName;

            File targetFile = new File(BASE_PATH + uniqueFileName);
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }

            try {
                file.transferTo(targetFile);
            } catch (Exception e) {
                throw new CommonException(400, "附件上传失败");
            }
            savedFiles.add(targetFile);

            FileAttachment attachment = new FileAttachment();
            attachment.setFileUrl(uniqueFileName);
            attachment.setFileName(originalFilename);
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setUploadedBy(context == null ? 1L : context.getId());
            attachments.add(attachment);
        }

        if (attachments.isEmpty()) {
            throw new CommonException(400, "资产附件不能为空");
        }

        return attachments;
    }

    private void createApprovalRecord(Long assetId) {
        LoginUserContext context = LoginUserInfoUtile.get();
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setApprovalType("ASSET");
        approvalRecord.setTargetType("asset");
        approvalRecord.setTargetId(assetId);
        approvalRecord.setApplicantId(context == null ? 1L : context.getId());
        approvalRecord.setApprovalStatus("PENDING");
        approvalRecord.setCreatedAt(LocalDateTime.now());
        approvalRecord.setUpdatedAt(LocalDateTime.now());
        approvalRecordMapper.insert(approvalRecord);
    }

    private void deleteSavedFiles(List<File> savedFiles) {
        for (File file : savedFiles) {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    // 生成单号方法
    private String generateOrderNo() {
        String orderOne = "AST";
        String orderSecond = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = orderOne + "-" + orderSecond + "-";
        LambdaQueryWrapper<Asset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(Asset::getAssetCode, prefix);
        queryWrapper.orderByDesc(Asset::getAssetCode);
        queryWrapper.last("limit 1");
        Asset asset = assetMapper.selectOne(queryWrapper);
        if (asset == null) {
            return orderOne + "-" + orderSecond + "-" + "000001";
        }

        String orderNo = asset.getAssetCode();
        String substring = orderNo.substring(orderNo.lastIndexOf("-") + 1);
        int num = Integer.parseInt(substring) + 1;
        return prefix + String.format("%06d", num);
    }
}
