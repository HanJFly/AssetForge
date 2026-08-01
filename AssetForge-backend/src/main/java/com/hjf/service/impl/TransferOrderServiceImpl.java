package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.*;
import com.hjf.mapper.*;
import com.hjf.param.LoginUserContext;
import com.hjf.param.TransferOrderDetailParam;
import com.hjf.param.TransferOrderPageParam;
import com.hjf.param.TransferOrderParam;
import com.hjf.service.ITransferOrderService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 转移单主表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class TransferOrderServiceImpl extends ServiceImpl<TransferOrderMapper, TransferOrder> implements ITransferOrderService {


    @Autowired
    private TransferOrderMapper transferOrderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private TransferOrderItemMapper transferOrderItemMapper;
    @Autowired
    private AssetMapper assetMapper;
    @Autowired
    private AssetCategoryMapper assetCategoryMapper;
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;
    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;
    @Autowired
    private ScrapOrderMapper scrapOrderMapper;
    @Autowired
    private ScrapOrderItemMapper scrapOrderItemMapper;
    @Autowired
    private ReturnOrderMapper returnOrderMapper;
    @Autowired
    private ReturnOrderItemMapper returnOrderItemMapper;
    //生成单号方法
    private String generateOrderNo() {
        String orderOne = "TRF";
        String orderSecond = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = orderOne + "-" + orderSecond+ "-" ;
        LambdaQueryWrapper<TransferOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(TransferOrder::getOrderNo, prefix);
        queryWrapper.orderByDesc(TransferOrder::getOrderNo);
        queryWrapper.last("limit 1");
        TransferOrder transferOrder = transferOrderMapper.selectOne(queryWrapper);
        if (transferOrder == null) {
            return prefix + "000001";
        }

        String orderNo = transferOrder.getOrderNo();
        String substring = orderNo.substring(orderNo.lastIndexOf("-") + 1);
        int num = Integer.parseInt(substring) + 1;
        return prefix + String.format("%06d", num);

    }
    /*
    * 创建转移单
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferOrderVO create(TransferOrderParam param) {

        //判断该转移记录是否已经存在
        int i = transferOrderMapper.queryTransferByParam(param);
        if (i > 0){
            throw new CommonException(400, "该转移记录已经存在");
        }
        TransferOrder transferOrder = new TransferOrder();
        //设置原用户信息
        transferOrder.setOrderNo(generateOrderNo());
        transferOrder.setFromUserId(param.getFromUserId());
        User from_user = userMapper.selectById(param.getFromUserId());
        transferOrder.setFromUserName(from_user.getRealName());
        transferOrder.setFromUserEmployeeNo(from_user.getEmployeeNo());
        transferOrder.setFromUserDepartmentId(from_user.getDepartmentId());
        Department from_department = departmentMapper.selectById(from_user.getDepartmentId());
        transferOrder.setFromUserDepartmentName(from_department.getName());
        //设置目标用户信息
        transferOrder.setToUserId(param.getToUserId());
        User to_user = userMapper.selectById(param.getToUserId());
        transferOrder.setToUserName(to_user.getRealName());
        transferOrder.setToUserEmployeeNo(to_user.getEmployeeNo());
        transferOrder.setToUserDepartmentId(to_user.getDepartmentId());
        Department to_department = departmentMapper.selectById(to_user.getDepartmentId());
        transferOrder.setToUserDepartmentName(to_department.getName());

        //设置其余信息
        transferOrder.setReason(param.getReason());
        transferOrder.setTransferDate(LocalDate.now());
        transferOrder.setApprovalStatus("PENDING");
        User manager = userMapper.selectById(from_department.getManagerUserId());
        transferOrder.setApproverName(manager != null ? manager.getRealName() : null);
        transferOrder.setCreatedAt(LocalDateTime.now());
        transferOrder.setUpdatedAt(LocalDateTime.now());
        transferOrderMapper.insert(transferOrder);

        //同步创建转移单资产明细表

        param.getItemList().forEach(item -> {
            // 检查该资产是否已有待审批的转移单
            LambdaQueryWrapper<TransferOrderItem> existsCheck = new LambdaQueryWrapper<>();
            existsCheck.eq(TransferOrderItem::getAssetId, item.getAssetId());
            TransferOrderItem exists = transferOrderItemMapper.selectOne(existsCheck);
            if (exists != null) {
                // 查对应的转移单是否还是 PENDING
                TransferOrder existsOrder = transferOrderMapper.selectById(exists.getOrderId());
                if ("PENDING".equals(existsOrder.getApprovalStatus())) {
                    throw new CommonException(400, "资产 " + item.getAssetId() + " 已有待审批的转移单");
                }
            }
            //检查是否在报废单中为待审批
            LambdaQueryWrapper<ScrapOrderItem> scrapCheck = new LambdaQueryWrapper<>();
            scrapCheck.eq(ScrapOrderItem::getAssetId, item.getAssetId());
            ScrapOrderItem scrapItem = scrapOrderItemMapper.selectOne(scrapCheck);
            if (scrapItem != null) {
                ScrapOrder scrapOrder = scrapOrderMapper.selectById(scrapItem.getOrderId());
                if ("PENDING".equals(scrapOrder.getApprovalStatus())) {
                    throw new CommonException(400, "该资产正在报废审批中");
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
            TransferOrderItem transferOrderItem = new TransferOrderItem();
            transferOrderItem.setOrderId(transferOrder.getId());
            transferOrderItem.setAssetId(item.getAssetId());
            Asset asset = assetMapper.selectById(item.getAssetId());
            if (asset.getIsDeleted() == 1){
                throw new CommonException(400, asset.getName()+ "该资产已被删除");
            }
            if (asset.getCurrentUserId() == null || !asset.getCurrentUserId().equals(param.getFromUserId())){
                throw new CommonException(400, asset.getName()+ "该资产不属于当前用户");
            }
            transferOrderItem.setAssetCode(asset.getAssetCode());
            transferOrderItem.setAssetName(asset.getName());
            AssetCategory assetCategory = assetCategoryMapper.selectById(asset.getCategoryId());
            transferOrderItem.setCategoryName(assetCategory.getName());
            transferOrderItem.setBrandModel(asset.getBrandModel());
            LambdaQueryWrapper<AssetLedger> ledgerQuery = new LambdaQueryWrapper<>();
            ledgerQuery.eq(AssetLedger::getAssetId, item.getAssetId());
            AssetLedger assetLedger = assetLedgerMapper.selectOne(ledgerQuery);
            if (assetLedger != null){
                transferOrderItem.setNetValueAtTransfer(assetLedger.getNetValue());
            }
            transferOrderItem.setCreatedAt(LocalDateTime.now());
            transferOrderItemMapper.insert(transferOrderItem);

        });


        //同步创建审批记录表
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setApprovalType("TRANSFER");
        approvalRecord.setTargetType("transfer_order");

        approvalRecord.setTargetId(transferOrder.getId());
        LoginUserContext context = LoginUserInfoUtile.get();
        approvalRecord.setApplicantId(context.getId());
        approvalRecord.setApproverId(from_department.getManagerUserId());
        approvalRecord.setTransferredTo(param.getToUserId());
        approvalRecord.setCreatedAt(LocalDateTime.now());
        approvalRecord.setUpdatedAt(LocalDateTime.now());
        approvalRecordMapper.insert(approvalRecord);

        TransferOrderVO transferOrderVO = new TransferOrderVO();
        transferOrderVO.setId(transferOrder.getId());
        transferOrderVO.setOrderNo(transferOrder.getOrderNo());
        transferOrderVO.setApprovalStatus(transferOrder.getApprovalStatus());
        return transferOrderVO;


    }

    /*
    * 转移单分页
    * */
    @Override
    public TransferOrderPageVO querypage(TransferOrderPageParam param) {
        PageHelper.startPage(param.getPage(),param.getSize());

        List<TransferOrderPageRecord> list = transferOrderMapper.querypage(param);
        Page<TransferOrderPageRecord> pageInfo = (Page<TransferOrderPageRecord>)list;
        TransferOrderPageVO transferOrderPageVO = new TransferOrderPageVO();
        transferOrderPageVO.setTotal(pageInfo.getTotal());
        transferOrderPageVO.setRecords(pageInfo);
        transferOrderPageVO.setSize(param.getSize());
        transferOrderPageVO.setPage(param.getPage());
        return transferOrderPageVO;
    }

    @Override
    public TransferOrderDetailVO detail(TransferOrderDetailParam param) {
        TransferOrder transferOrder = transferOrderMapper.selectById(param.getId());
        if(transferOrder == null){
            throw new CommonException(404, "转移单不存在");
        }
        TransferOrderDetailVO transferOrderDetailVO = new TransferOrderDetailVO();
        transferOrderDetailVO.setId(transferOrder.getId());
        transferOrderDetailVO.setOrderNo(transferOrder.getOrderNo());
        transferOrderDetailVO.setFromDepartmentId(transferOrder.getFromUserDepartmentId());
        transferOrderDetailVO.setFromDepartmentName(transferOrder.getFromUserDepartmentName());
        transferOrderDetailVO.setToDepartmentId(transferOrder.getToUserDepartmentId());
        transferOrderDetailVO.setToDepartmentName(transferOrder.getToUserDepartmentName());
        transferOrderDetailVO.setFromUserId(transferOrder.getFromUserId());
        transferOrderDetailVO.setFromUserName(transferOrder.getFromUserName());
        transferOrderDetailVO.setToUserId(transferOrder.getToUserId());
        transferOrderDetailVO.setToUserName(transferOrder.getToUserName());
        transferOrderDetailVO.setReason(transferOrder.getReason());
        transferOrderDetailVO.setApprovalStatus(transferOrder.getApprovalStatus());

        //查询转移单资产明细表
        LambdaQueryWrapper<TransferOrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransferOrderItem::getOrderId,transferOrder.getId());
        List<TransferOrderItem> itemList = transferOrderItemMapper.selectList(queryWrapper);
        if(itemList != null){
            List<TransferOrderDetailItemVO> transferOrderDetailItemVOList = itemList.stream().map(item -> {
                TransferOrderDetailItemVO transferOrderDetailItemVO = new TransferOrderDetailItemVO();
                transferOrderDetailItemVO.setAssetId(item.getAssetId());
                transferOrderDetailItemVO.setAssetCode(item.getAssetCode());
                transferOrderDetailItemVO.setAssetName(item.getAssetName());
                return transferOrderDetailItemVO;
            }).collect(Collectors.toList());
            transferOrderDetailVO.setItemList(transferOrderDetailItemVOList);
        }
        return transferOrderDetailVO;


    }
}
