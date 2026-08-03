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
import com.hjf.context.LoginUserIRoleUtile;
import com.hjf.entity.ApprovalRecord;
import com.hjf.entity.Asset;
import com.hjf.entity.AssetCategory;
import com.hjf.entity.Department;
import com.hjf.entity.FileAttachment;
import com.hjf.entity.InventoryDetail;
import com.hjf.entity.LossOrder;
import com.hjf.entity.RequisitionOrderItem;
import com.hjf.entity.Role;
import com.hjf.entity.ReturnOrderItem;
import com.hjf.entity.ScrapOrderItem;
import com.hjf.entity.TransferOrderItem;
import com.hjf.entity.User;
import com.hjf.entity.UserRole;
import com.hjf.mapper.ApprovalRecordMapper;
import com.hjf.mapper.AssetCategoryMapper;
import com.hjf.mapper.AssetMapper;
import com.hjf.mapper.DepartmentMapper;
import com.hjf.mapper.FileAttachmentMapper;
import com.hjf.mapper.InventoryDetailMapper;
import com.hjf.mapper.LossOrderMapper;
import com.hjf.mapper.RequisitionOrderItemMapper;
import com.hjf.mapper.ReturnOrderItemMapper;
import com.hjf.mapper.RoleMapper;
import com.hjf.mapper.ScrapOrderItemMapper;
import com.hjf.mapper.TransferOrderItemMapper;
import com.hjf.mapper.UserMapper;
import com.hjf.mapper.UserRoleMapper;
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

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private LoginUserIRoleUtile loginUserIRoleUtile;

    /*
    * 分页查询
    * */
    @Override
    public AssetPageVO queryPage(AssetPageParam param) {
        // 资产分页查询入口。
        // 这里先按当前角色收口查询范围，避免前端被绕过后仍能查到越权数据。
        applyAssetQueryScope(param);
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
        // 详情读取同样要校验资产归属，防止通过接口直接查看其他部门资产。
        validateAssetReadAccess(asset);

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
        // 新建前强制收口部门字段：
        // 部门管理员只能登记到本人部门，资产管理员保留全局能力。
        enforceAssetDepartmentScope(param);
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

        LambdaQueryWrapper<Department> qw = new LambdaQueryWrapper<>();
        qw.eq(Department::getId, param.getDepartmentId());
        Department departmentManager = departmentMapper.selectOne(qw);
        if (departmentManager == null || departmentManager.getManagerUserId() == null) {
            throw new CommonException(400, "资产所属部门未配置部门管理员，无法提交审批");
        }
        approvalRecord.setApproverId(departmentManager.getManagerUserId());
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
        // 带附件登记和普通登记走同一套部门权限规则，避免绕过前端跨部门建资产。
        enforceAssetDepartmentScope(param);
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
        // 修改时分两步控制：
        // 先校验当前人能不能操作这条资产，再强制覆盖提交部门，避免改到别的部门名下。
        validateAssetWriteAccess(asset);
        enforceAssetDepartmentScope(param);
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
        // 删除属于写操作，必须先校验资产归属，避免删除其他部门资产。
        validateAssetWriteAccess(asset);
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
        if (asset == null) {
            throw new CommonException(404, "未找到资产");
        }
        // 条码详情本质上也是资产详情，沿用同一套读取权限校验。
        validateAssetReadAccess(asset);
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
        // 加上审批人：从部门的管理员获取
        Asset asset = assetMapper.selectById(assetId);
        if (asset != null && asset.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(asset.getDepartmentId());
            if (department == null || department.getManagerUserId() == null) {
                throw new CommonException(400, "资产所属部门未配置部门管理员，无法提交审批");
            }
            approvalRecord.setApproverId(department.getManagerUserId());
        }
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

    // 从线程上下文中获取当前登录身份。
    // 后续所有资产权限判断都以这里拿到的登录信息为起点。
    private LoginUserContext requireLoginUser() {
        LoginUserContext context = LoginUserInfoUtile.get();
        if (context == null || context.getId() == null) {
            throw new CommonException(401, "未登录或登录已过期");
        }
        return context;
    }

    // 根据登录身份查询完整用户实体。
    // 主要用于拿部门、删除标记等信息，供权限判断复用。
    private User requireCurrentUser() {
        LoginUserContext context = requireLoginUser();
        User user = userMapper.selectById(context.getId());
        if (user == null || user.getIsDeleted() == 1) {
            throw new CommonException(401, "当前登录用户不存在");
        }
        return user;
    }



    // 判断用户是否拥有指定角色编码。
    // 先尝试读取当前选中角色，读不到时再回落到用户的全部角色。
    private boolean hasRoleCode(Long userId, String roleCode) {
        String selectedRoleId = loginUserIRoleUtile.getRole(String.valueOf(userId));
        if (selectedRoleId != null && !selectedRoleId.isBlank()) {
            try {
                Role selectedRole = roleMapper.selectById(Long.valueOf(selectedRoleId));
                if (selectedRole != null && roleCode.equals(selectedRole.getCode())) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
        }


        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(Role::getId, userRoles.stream().map(UserRole::getRoleId).toList());
        List<Role> roles = roleMapper.selectList(roleWrapper);
        return roles != null && roles.stream().anyMatch(role -> roleCode.equals(role.getCode()));
    }

    // 判断是否为资产管理员：资产模块的全局权限入口。
    private boolean isAssetAdmin(Long userId) {
        return hasRoleCode(userId, "ASSET_ADMIN");
    }

    // 判断是否为部门管理员：资产模块的部门级权限入口。
    private boolean isDeptManager(Long userId) {
        return hasRoleCode(userId, "DEPT_MANAGER");
    }

    private boolean isStorekeeper(Long userId) {
        return hasRoleCode(userId, "STOREKEEPER");
    }

    // 给资产分页查询自动套上数据范围。
    // 资产管理员看全部，部门管理员按 departmentId 过滤，普通员工按 currentUserId 过滤。
    private void applyAssetQueryScope(AssetPageParam param) {
        User currentUser = requireCurrentUser();
        if (isAssetAdmin(currentUser.getId())) {
            return;
        }

        if (isStorekeeper(currentUser.getId())) {
            param.setCurrentUserId(null);
            param.setDepartmentId(null);
            param.setDepartmentName(null);
            param.setAssetStatus("STOCK");
            return;
        }

        if (isDeptManager(currentUser.getId())) {
            if (currentUser.getDepartmentId() == null) {
                throw new CommonException(400, "当前部门管理员未绑定所属部门");
            }
            param.setDepartmentId(currentUser.getDepartmentId());
            param.setDepartmentName(null);
            param.setCurrentUserId(null);
            return;
        }

        param.setCurrentUserId(currentUser.getId());
        param.setDepartmentId(null);
    }

    // 校验当前人是否允许查看这条资产。
    // 用于详情、条码详情等读取场景，防止跨部门或跨用户越权访问。
    private void validateAssetReadAccess(Asset asset) {
        User currentUser = requireCurrentUser();
        if (isAssetAdmin(currentUser.getId())) {
            return;
        }

        if (isDeptManager(currentUser.getId())) {
            if (currentUser.getDepartmentId() == null) {
                throw new CommonException(400, "当前部门管理员未绑定所属部门");
            }
            if (asset.getDepartmentId() == null || !asset.getDepartmentId().equals(currentUser.getDepartmentId())) {
                throw new CommonException(403, "无权查看其他部门资产");
            }
            return;
        }

        if (asset.getCurrentUserId() == null || !asset.getCurrentUserId().equals(currentUser.getId())) {
            throw new CommonException(403, "无权查看他人资产");
        }
    }

    // 校验当前人是否允许操作这条资产。
    // 用于修改、删除等写场景，保证部门管理员只能处理本部门资产。
    private void validateAssetWriteAccess(Asset asset) {
        User currentUser = requireCurrentUser();
        if (isAssetAdmin(currentUser.getId())) {
            return;
        }

        if (isDeptManager(currentUser.getId())) {
            if (currentUser.getDepartmentId() == null) {
                throw new CommonException(400, "当前部门管理员未绑定所属部门");
            }
            if (asset.getDepartmentId() == null || !asset.getDepartmentId().equals(currentUser.getDepartmentId())) {
                throw new CommonException(403, "无权操作其他部门资产");
            }
            return;
        }

        throw new CommonException(403, "当前角色无权操作资产");
    }

    // 对普通 JSON 资产表单强制收口部门字段。
    // 部门管理员会被后端自动覆盖为本人部门，资产管理员保留原始提交值。
    private void enforceAssetDepartmentScope(AssetParam param) {
        User currentUser = requireCurrentUser();
        if (isAssetAdmin(currentUser.getId())) {
            return;
        }

        if (isDeptManager(currentUser.getId())) {
            if (currentUser.getDepartmentId() == null) {
                throw new CommonException(400, "当前部门管理员未绑定所属部门");
            }
            param.setDepartmentId(currentUser.getDepartmentId());
            param.setDepartmentName(currentUser.getDepartmentName());
            return;
        }

        throw new CommonException(403, "当前角色无权登记或修改资产");
    }

    // 对“带附件上传”的资产登记表单执行同样的部门收口规则。
    // 单独重载是因为 create-with-files 使用的是另一套参数对象。
    private void enforceAssetDepartmentScope(AssetCreateWithFilesParam param) {
        User currentUser = requireCurrentUser();
        if (isAssetAdmin(currentUser.getId())) {
            return;
        }

        if (isDeptManager(currentUser.getId())) {
            if (currentUser.getDepartmentId() == null) {
                throw new CommonException(400, "当前部门管理员未绑定所属部门");
            }
            param.setDepartmentId(currentUser.getDepartmentId());
            return;
        }

        throw new CommonException(403, "当前角色无权登记资产");
    }
}
