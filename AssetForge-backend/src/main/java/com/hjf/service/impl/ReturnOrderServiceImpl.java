package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.*;
import com.hjf.entity.ReturnOrder;
import com.hjf.mapper.*;
import com.hjf.param.*;
import com.hjf.service.IReturnOrderService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 归还单主表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class ReturnOrderServiceImpl extends ServiceImpl<ReturnOrderMapper, ReturnOrder> implements IReturnOrderService {

    @Autowired
    private ReturnOrderMapper returnOrderMapper;
    @Autowired
    private ReturnOrderItemMapper returnOrderItemMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AssetMapper assetMapper;
    @Autowired
    private AssetCategoryMapper assetCategoryMapper;
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;
    @Autowired
    private TransferOrderMapper transferOrderMapper;
    @Autowired
    private TransferOrderItemMapper transferOrderItemMapper;
    @Autowired
    private ScrapOrderMapper scrapOrderMapper;
    @Autowired
    private ScrapOrderItemMapper scrapOrderItemMapper;

    //生成单号方法
    private String generateOrderNo() {
        String orderOne = "RET";
        String orderSecond = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = orderOne + "-" + orderSecond+ "-" ;
        LambdaQueryWrapper<ReturnOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(ReturnOrder::getOrderNo, prefix);
        queryWrapper.orderByDesc(ReturnOrder::getOrderNo);
        queryWrapper.last("limit 1");
        ReturnOrder transferOrder = returnOrderMapper.selectOne(queryWrapper);
        if (transferOrder == null) {
            return prefix + "000001";
        }

        String orderNo = transferOrder.getOrderNo();
        String substring = orderNo.substring(orderNo.lastIndexOf("-") + 1);
        int num = Integer.parseInt(substring) + 1;
        return prefix + String.format("%06d", num);

    }
    /*
    * 创建归还单
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnOrderCreateVO create(ReturnOrderCreateParam param) {
        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.setOrderNo(generateOrderNo());
        LoginUserContext context = LoginUserInfoUtile.get();
        returnOrder.setReturnUserId(context.getId());
        returnOrder.setReturnUserName(context.getRealName());
        User user = userMapper.selectById(context.getId());
        returnOrder.setReturnUserEmployeeNo(user.getEmployeeNo());
        returnOrder.setReturnUserDepartmentId(user.getDepartmentId());
        returnOrder.setReturnUserDepartmentName(user.getDepartmentName());
        returnOrder.setReason(param.getReason());
        returnOrder.setExpectedReturnDate(param.getExpectedReturnDate());
        returnOrder.setApprovedAt(LocalDateTime.now());
        returnOrder.setCreatedAt(LocalDateTime.now());
        returnOrder.setUpdatedAt(LocalDateTime.now());
        returnOrder.setOrderStatus("PENDING");  // 创建时待入库
        returnOrderMapper.insert(returnOrder);
        //更新归还资产明细表，归还资产不用审批，所以不用新建审批记录表了
        if(param.getItemList() != null && param.getItemList().size() > 0){
            param.getItemList().forEach(item -> {
                Asset asset = assetMapper.selectById(item.getAssetId());
                if(asset.getCurrentUserId() == null || !asset.getCurrentUserId().equals(context.getId())){
                    throw new CommonException(400, "该资产不属于当前领用人");
                }
                //检查资产是否在其他流程中是待审批状态
                //检查是否在转移单中为待审批
                LambdaQueryWrapper<TransferOrderItem> queryTransferOrderItem = new LambdaQueryWrapper<>();
                queryTransferOrderItem.eq(TransferOrderItem::getAssetId, item.getAssetId());
                TransferOrderItem transferOrderItem = transferOrderItemMapper.selectOne(queryTransferOrderItem);
                if(transferOrderItem != null){
                   TransferOrder transferOrder = transferOrderMapper.selectById(transferOrderItem.getOrderId());
                   if(transferOrder.getApprovalStatus().equals("PENDING")){
                       throw new CommonException(400, "该资产在转移流程中，不能归还");
                   }

                }
                //检查是否在报废单中为待审批
                LambdaQueryWrapper<ScrapOrderItem> scrapCheck = new LambdaQueryWrapper<>();
                scrapCheck.eq(ScrapOrderItem::getAssetId, item.getAssetId());
                ScrapOrderItem scrapItem = scrapOrderItemMapper.selectOne(scrapCheck);
                if (scrapItem != null) {
                    ScrapOrder scrapOrder = scrapOrderMapper.selectById(scrapItem.getOrderId());
                    if ("PENDING".equals(scrapOrder.getApprovalStatus())) {
                        throw new CommonException(400, "该资产正在报废审批中，无法归还");
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

                ReturnOrderItem returnOrderItem = new ReturnOrderItem();
                returnOrderItem.setOrderId(returnOrder.getId());
                returnOrderItem.setAssetId(item.getAssetId());

                returnOrderItem.setAssetCode(asset.getAssetCode());
                returnOrderItem.setAssetName(asset.getName());
                AssetCategory assetCategory = assetCategoryMapper.selectById(asset.getCategoryId());
                returnOrderItem.setCategoryName(assetCategory.getName());
                returnOrderItem.setConditionRemark(item.getConditionRemark());
                returnOrderItem.setAssetCondition(item.getAssetCondition());
                LambdaQueryWrapper<AssetLedger> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AssetLedger::getAssetId, item.getAssetId());
                returnOrderItem.setNetValueAtReturn(assetLedgerMapper.selectOne(queryWrapper).getNetValue());
                returnOrderItem.setCreatedAt(LocalDateTime.now());
                returnOrderItemMapper.insert(returnOrderItem);
            });



        }
        ReturnOrderCreateVO vo = new ReturnOrderCreateVO();
        vo.setId(returnOrder.getId());
        vo.setOrderNo(returnOrder.getOrderNo());
        vo.setApprovalStatus(returnOrder.getApprovalStatus());
        return vo;
    }

    @Override
    public ReturnOrderPageVO queryPage(ReturnOrderPageParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        List<ReturnOrderPageRecord> returnOrders = returnOrderMapper.queryPage(param);
        Page<ReturnOrderPageRecord> pageInfo = (Page<ReturnOrderPageRecord>) returnOrders;

        ReturnOrderPageVO vo = new ReturnOrderPageVO();
        vo.setRecords(pageInfo);
        vo.setTotal(pageInfo.getTotal());
        vo.setPage(pageInfo.getPageNum());
        vo.setSize(pageInfo.getPageSize());
        return vo;
    }

    /*
    * 归还单详情
    * */
    @Override
    public ReturnOrderDetailVO datail(ReturnOrderDetailParam param) {
        ReturnOrder returnOrder = returnOrderMapper.selectById(param.getId());
        LambdaQueryWrapper<ReturnOrderItem> queryList = new LambdaQueryWrapper<>();
        queryList.eq(ReturnOrderItem::getOrderId,returnOrder.getId());

        ReturnOrderDetailVO vo = new ReturnOrderDetailVO();
        vo.setId(returnOrder.getId());
        vo.setOrderNo(returnOrder.getOrderNo());
        vo.setReason(returnOrder.getReason());
        vo.setApprovalStatus(returnOrder.getApprovalStatus());
        List<ReturnOrderItem> itemList = returnOrderItemMapper.selectList(queryList);
        if (itemList.isEmpty()){
            throw new CommonException(404, "该归还单没有资产信息");
        }
        itemList.forEach(item ->{
           ReturnOrderDetaiItemVO returnItem = new ReturnOrderDetaiItemVO();
            returnItem.setAssetID(item.getAssetId());
            returnItem.setAssetCode(item.getAssetCode());
            returnItem.setAssetName(item.getAssetName());
            returnItem.setReturnCondition(item.getAssetCondition());
            returnItem.setConditionRemark(item.getConditionRemark());
            vo.getItemList().add(returnItem);

        });

        return vo;
    }

    /*
    * 入库确认
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnOrderConfirmInboundVO inbound(ReturnOrderConfirmInboundParam param) {
        ReturnOrder returnOrder = returnOrderMapper.selectById(param.getId());
        if(returnOrder == null){
            throw new CommonException(404, "归还单不存在");
        }
        if(!"PENDING".equals(returnOrder.getOrderStatus())){
            throw new CommonException(400, "该归还单状态不是待入库");
        }
        LoginUserContext context = LoginUserInfoUtile.get();
        returnOrder.setReceiverName(context.getRealName());
        returnOrder.setReceivedAt(LocalDateTime.now());
        returnOrder.setConfirmRemark(param.getConfirmRemark());
        returnOrder.setOrderStatus("COMPLETED");
        returnOrderMapper.update(returnOrder, new LambdaQueryWrapper<ReturnOrder>().eq(ReturnOrder::getId,param.getId()));

        //更新归还单资产明细表
        LambdaQueryWrapper<ReturnOrderItem> queryItem= new LambdaQueryWrapper<>();
        queryItem.eq(ReturnOrderItem::getOrderId,returnOrder.getId());
        List<ReturnOrderItem> returnOrderItems = returnOrderItemMapper.selectList(queryItem);
        if (returnOrderItems.isEmpty()){
            throw new CommonException(404, "该归还单没有资产信息");
        }
        returnOrderItems.forEach(item->{
            Asset asset = assetMapper.selectById(item.getAssetId());
            if(asset == null){
                throw new CommonException(404, "该资产不存在");
            }
            if(asset.getIsDeleted() != 0){
                throw new CommonException(400, "该资产已被删除");
            }
            LambdaUpdateWrapper<Asset> queryUpdate = new LambdaUpdateWrapper<>();
            queryUpdate.eq(Asset::getId,item.getAssetId());
            queryUpdate.set(Asset::getCurrentUserId,null);
            queryUpdate.set(Asset::getStatus, "STOCK");
            queryUpdate.set(Asset::getLocation, param.getStorageLocation());
            assetMapper.update(null, queryUpdate);




        });

        ReturnOrderConfirmInboundVO vo = new ReturnOrderConfirmInboundVO();
        vo.setSuccess(true);
        vo.setOrderStatus("COMPLETED");
        return vo;



    }
}
