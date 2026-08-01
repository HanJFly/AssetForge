package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.*;
import com.hjf.mapper.*;
import com.hjf.param.*;
import com.hjf.service.IFileAttachmentService;
import com.hjf.service.IScrapOrderItemService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 报废单-资产明细表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class ScrapOrderItemServiceImpl extends ServiceImpl<ScrapOrderItemMapper, ScrapOrderItem> implements IScrapOrderItemService {

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AssetCategoryMapper assetCategoryMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;
    @Autowired
    private TransferOrderMapper transferOrderMapper;
    @Autowired
    private TransferOrderItemMapper transferOrderItemMapper;
    @Autowired
    private ReturnOrderMapper returnOrderMapper;
    @Autowired
    private ReturnOrderItemMapper returnOrderItemMapper;
    @Autowired
    private ScrapOrderMapper scrapOrderMapper;
    @Autowired
    private ScrapOrderItemMapper  scrapOrderItemMapper;
    @Autowired
    private IFileAttachmentService fileAttachmentService;
    @Autowired
    private FileAttachmentMapper fileAttachmentMapper;

    //生成单号方法
    private String generateOrderNo() {
        String orderOne = "SCR";
        String orderSecond = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = orderOne + "-" + orderSecond+ "-" ;
        LambdaQueryWrapper<ScrapOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(ScrapOrder::getOrderNo, prefix);
        queryWrapper.orderByDesc(ScrapOrder::getOrderNo);
        queryWrapper.last("limit 1");
        ScrapOrder scrapOrder = scrapOrderMapper.selectOne(queryWrapper);
        if (scrapOrder == null) {
            return prefix + "000001";
        }

        String orderNo = scrapOrder.getOrderNo();
        String substring = orderNo.substring(orderNo.lastIndexOf("-") + 1);
        int num = Integer.parseInt(substring) + 1;
        return prefix + String.format("%06d", num);

    }


    /*
    * 创建报废单
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScrapOrderCreateVO create(ScrapOrderCreateParam param) {
        //1新建报废单
        ScrapOrder scrapOrder = new ScrapOrder();
        scrapOrder.setOrderNo(generateOrderNo());
        LoginUserContext context =  LoginUserInfoUtile.get();
        scrapOrder.setApplicantId(context.getId());
        scrapOrder.setApplicantName(context.getRealName());
        User user = userMapper.selectById(context.getId());
        scrapOrder.setApplicantEmployeeNo(user.getEmployeeNo());
        scrapOrder.setApplicantDepartmentId(user.getDepartmentId());
        scrapOrder.setApplicantDepartmentName(user.getDepartmentName());
        scrapOrder.setReason(param.getReason());
        //设置估计残值，先把写，残值率在明细表里汇总后再设置
        /*LambdaQueryWrapper<AssetLedger> queryAssetLedger = new LambdaQueryWrapper<>();
        queryAssetLedger.eq(AssetLedger::getAssetId,item.getAssetId());
        AssetLedger assetLedger = assetLedgerMapper.selectOne(queryAssetLedger);
        if(assetLedger != null){
            BigDecimal multiply = assetLedger.getOriginalValue().multiply(assetLedger.getResidualRate());
            scrapOrder.setEstimatedResidualValue(multiply);
        }*/
        scrapOrder.setApprovalStatus("PENDING");
        scrapOrder.setCreatedAt(LocalDateTime.now());
        scrapOrder.setUpdatedAt(LocalDateTime.now());
        scrapOrderMapper.insert(scrapOrder);

        //2.新建报废资产明细表
        BigDecimal totalResidual = BigDecimal.ZERO;
        for (ScrapOrderCreateParam.Item item : param.getItemList()) {
            Asset asset = assetMapper.selectById(item.getAssetId());
            //判断资产是否存在
            if(asset == null){
                throw new CommonException(404, "资产不存在");
            }
            if(asset.getIsDeleted() != 0){
                throw new CommonException(400, "资产已删除");
            }
            if("SCRAPPED".equals(asset.getStatus())){
                throw new CommonException(400, "资产已报废，不能重复报废");
            }
            if("LOST".equals(asset.getStatus())){
                throw new CommonException(400, "资产已盘亏，不能报废");
            }
            //检查资产是否在其他流程中是待审批状态
            //检查是否在转移单中为待审批
            LambdaQueryWrapper<TransferOrderItem> queryTransferOrderItem = new LambdaQueryWrapper<>();
            queryTransferOrderItem.eq(TransferOrderItem::getAssetId, item.getAssetId());
            TransferOrderItem transferOrderItem = transferOrderItemMapper.selectOne(queryTransferOrderItem);
            if(transferOrderItem != null){
                TransferOrder transferOrder = transferOrderMapper.selectById(transferOrderItem.getOrderId());
                if(transferOrder.getApprovalStatus().equals("PENDING")){
                    throw new CommonException(400, "该资产在转移流程中，不能报废");
                }

            }
            //检查是否在报废单中为待审批
            LambdaQueryWrapper<ScrapOrderItem> scrapCheck = new LambdaQueryWrapper<>();
            scrapCheck.eq(ScrapOrderItem::getAssetId, item.getAssetId());
            ScrapOrderItem scrapItem = scrapOrderItemMapper.selectOne(scrapCheck);
            if (scrapItem != null) {
                ScrapOrder scrapOrder1 = scrapOrderMapper.selectById(scrapItem.getOrderId());
                if ("PENDING".equals(scrapOrder1.getApprovalStatus())) {
                    throw new CommonException(400, "该资产正在报废审批中，不能报废");
                }
            }
            //检查是否在归还单中为待审批
            LambdaQueryWrapper<ReturnOrderItem> returnCheck = new LambdaQueryWrapper<>();
            returnCheck.eq(ReturnOrderItem::getAssetId, item.getAssetId());
            ReturnOrderItem returnItem = returnOrderItemMapper.selectOne(returnCheck);
            if (returnItem != null) {
                ReturnOrder returnOrder1 = returnOrderMapper.selectById(returnItem.getOrderId());
                if ("PENDING".equals(returnOrder1.getApprovalStatus())) {
                    throw new CommonException(400, "该资产已有待审批的归还单");
                }
            }
            ScrapOrderItem scrapOrderItem = new ScrapOrderItem();
            scrapOrderItem.setOrderId(scrapOrder.getId());
            scrapOrderItem.setAssetId(item.getAssetId());
            scrapOrderItem.setAssetCode(asset.getAssetCode());
            scrapOrderItem.setAssetName(asset.getName());
            scrapOrderItem.setCategoryName(assetCategoryMapper.selectById(asset.getCategoryId()).getName());
            LambdaQueryWrapper<AssetLedger> queryAssetLedgerM = new LambdaQueryWrapper<>();
            queryAssetLedgerM.eq(AssetLedger::getAssetId,item.getAssetId());
            AssetLedger assetLedgerM = assetLedgerMapper.selectOne(queryAssetLedgerM);
            scrapOrderItem.setLedgerNo(assetLedgerM.getLedgerNo());
            scrapOrderItem.setOriginalValueAtScrap(assetLedgerM.getOriginalValue());
            scrapOrderItem.setNetValueAtScrap(assetLedgerM.getNetValue());
            scrapOrderItem.setAccumulatedDepreciationAtScrap(assetLedgerM.getAccumulatedDepreciation());
            scrapOrderItem.setCreatedAt(LocalDateTime.now());
            scrapOrderItemMapper.insert(scrapOrderItem);
            BigDecimal multiply = assetLedgerM.getOriginalValue().multiply(assetLedgerM.getResidualRate());
            totalResidual = totalResidual.add(multiply);
        }

        scrapOrder.setEstimatedResidualValue(totalResidual);
        scrapOrderMapper.updateById(scrapOrder);


        //绑定上传的附件
        // 绑定附件
         param.getAttachmentIds().forEach(id ->{
             FileAttachmentBindParam fileAttachmentBindParam = new FileAttachmentBindParam();
             fileAttachmentBindParam.setAttachmentIds(id);
             fileAttachmentBindParam.setBizType("SCRAP_PHOTO");
             fileAttachmentBindParam.setBizId(scrapOrder.getId());
             fileAttachmentService.bindFile(fileAttachmentBindParam);
         });





        //新建审批记录表
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setApprovalType("SCARP");
        approvalRecord.setTargetType("scrap_order");
        approvalRecord.setTargetId(scrapOrder.getId());
        approvalRecord.setApplicantId(context.getId());
        Department department = departmentMapper.selectById(user.getDepartmentId());
        approvalRecord.setApproverId(department.getManagerUserId());
        approvalRecord.setApprovalStatus("PENDING");
        approvalRecord.setCreatedAt(LocalDateTime.now());
        approvalRecord.setUpdatedAt(LocalDateTime.now());
        approvalRecordMapper.insert(approvalRecord);


        //向前端返回vo
        ScrapOrderCreateVO vo = new ScrapOrderCreateVO();
        vo.setId(scrapOrder.getId());
        vo.setOrderNo(scrapOrder.getOrderNo());
        vo.setApprovalStatus(scrapOrder.getApprovalStatus());
        return vo;

    }


    /*
    * 分页查询
    * */
    @Override
    public ScrapOrderPageVO queryPage(ScrapOrderPageParam param) {
        PageHelper.startPage(param.getPage(),param.getSize());
        List<ScrapOrderPageRecord> list = scrapOrderMapper.queryPage(param);
        Page<ScrapOrderPageRecord> pageInfo = (Page<ScrapOrderPageRecord>) list;
        ScrapOrderPageVO vo = new ScrapOrderPageVO();
        vo.setPage(pageInfo.getPageNum());
        vo.setSize(pageInfo.getPageSize());
        vo.setTotal(pageInfo.getTotal());
        vo.setRecords(pageInfo);
        return vo;
    }

    /*
    * 报废单详情
    * */
    @Override
    public ScrapOrderDetailVO detail(ScrapOrderDetailParam param) {
        ScrapOrder scrapOrder = scrapOrderMapper.selectById(param.getId());
        if (scrapOrder == null) {
            throw new CommonException(404, "该报废单不存在");
        }
        ScrapOrderDetailVO detailVO = new ScrapOrderDetailVO();
        detailVO.setId(scrapOrder.getId());
        detailVO.setOrderNo(scrapOrder.getOrderNo());
        detailVO.setReason(scrapOrder.getReason());
        detailVO.setApprovalStatus(scrapOrder.getApprovalStatus());
        //设置报废单资产明细表
        LambdaQueryWrapper<ScrapOrderItem> queryItem = new LambdaQueryWrapper<>();
        queryItem.eq(ScrapOrderItem::getOrderId, scrapOrder.getId());
        List<ScrapOrderItem> itemList = scrapOrderItemMapper.selectList(queryItem);
        if (!itemList.isEmpty()) {
            for (ScrapOrderItem item : itemList) {
                ScrapOrderItemDetailVO itemDetailVO = new ScrapOrderItemDetailVO();
                itemDetailVO.setAssetId(item.getAssetId());
                itemDetailVO.setAssetCode(item.getAssetCode());
                itemDetailVO.setAssetName(item.getAssetName());
                detailVO.getItemList().add(itemDetailVO);
            }
        }
        //设置附件列表
        LambdaQueryWrapper<FileAttachment> queryFile = new LambdaQueryWrapper<>();
        queryFile.eq(FileAttachment::getBizType, "SCRAP_PHOTO");
        queryFile.eq(FileAttachment::getBizId, scrapOrder.getId());
        List<FileAttachment> fileAttachmentList = fileAttachmentMapper.selectList(queryFile);
        if (!fileAttachmentList.isEmpty()) {
            for (FileAttachment attachment : fileAttachmentList) {
                ScrapOrderDetailFileVO fileVO = new ScrapOrderDetailFileVO();
                fileVO.setId(attachment.getId());
                fileVO.setFileName(attachment.getFileName());
                fileVO.setFileUrl(attachment.getFileUrl());
                detailVO.getAttachmentList().add(fileVO);
            }
        }

        return detailVO;
    }


}
