package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.SelectCount;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.*;
import com.hjf.mapper.*;
import com.hjf.param.*;
import com.hjf.service.IRequisitionOrderService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.*;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.sql.ast.spi.SqlAstQueryPartProcessingState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 申领单主表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class RequisitionOrderServiceImpl extends ServiceImpl<RequisitionOrderMapper, RequisitionOrder> implements IRequisitionOrderService {

    @Autowired
    private RequisitionOrderMapper requisitionOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AssetCategoryMapper assetCategoryMapper;

    @Autowired
    private RequisitionOrderItemMapper requisitionOrderItemMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;
    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private TransferOrderItemMapper transferOrderItemMapper;
    @Autowired
    private TransferOrderMapper transferOrderMapper;
    @Autowired
    private ScrapOrderItemMapper scrapOrderItemMapper;
    @Autowired
    private ScrapOrderMapper scrapOrderMapper;
    @Autowired
    private ReturnOrderItemMapper returnOrderItemMapper;
    @Autowired
    private ReturnOrderMapper returnOrderMapper;
    //生成单号方法
    private String generateOrderNo() {
        String orderOne = "REQ";
        String orderSecond = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = orderOne + "-" + orderSecond + "-";
        LambdaQueryWrapper<RequisitionOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(RequisitionOrder::getOrderNo, prefix);
        queryWrapper.orderByDesc(RequisitionOrder::getOrderNo);
        queryWrapper.last("limit 1");
        RequisitionOrder requisitionOrder = requisitionOrderMapper.selectOne(queryWrapper);
        if (requisitionOrder == null) {
            return orderOne + "-" + orderSecond+ "-" + "000001";
        }

        String orderNo = requisitionOrder.getOrderNo();
        String substring = orderNo.substring(orderNo.lastIndexOf("-") + 1);
        int num = Integer.parseInt(substring) + 1;
        return prefix + String.format("%06d", num);

    }
    /*
    * 创建
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RequisitionOrderCreateVO create(RequisitionOrderCreateParam param) {



        RequisitionOrder requisitionOrder = new RequisitionOrder();
        //设置单号
        requisitionOrder.setOrderNo(generateOrderNo());
        //设置申领人id
        LoginUserContext context = LoginUserInfoUtile.get();
        requisitionOrder.setApplicantId(context.getId());
        //设置申领人姓名
        requisitionOrder.setApplicantName(context.getRealName());
        //设置申领人工号
        User user = userMapper.selectById(context.getId());
        requisitionOrder.setApplicantEmployeeNo(user.getEmployeeNo());
        //设置申领人部门id
        requisitionOrder.setApplicantDepartmentId(user.getDepartmentId());
        //设置申领人部门名称
        requisitionOrder.setApplicantDepartmentName(user.getDepartmentName());
        //设置申领原因
        requisitionOrder.setReason(param.getReason());
        //设置expected_date
        requisitionOrder.setExpectedDate(param.getExpectedDate());
        //设置approval_status
        requisitionOrder.setApprovalStatus("PENDING");
        //设置申领单状态
        requisitionOrder.setOrderStatus("PENDING");
        //设置create_time
        requisitionOrder.setCreatedAt(LocalDateTime.now());
        //设置update_time
        requisitionOrder.setUpdatedAt(LocalDateTime.now());
        //新建申领单
        int insert = requisitionOrderMapper.insert(requisitionOrder);


        //如果新建成功的话新建申领单资产明细及新建审批记录表
        if (insert > 0 ){
            //根据单号获取刚刚新建的申领单id
            LambdaQueryWrapper<RequisitionOrder> getIdByOrderNo = new LambdaQueryWrapper<>();
            getIdByOrderNo.eq(RequisitionOrder::getOrderNo,requisitionOrder.getOrderNo());
            RequisitionOrder requisitionOrder1 = requisitionOrderMapper.selectOne(getIdByOrderNo);

            //根据资产分类id获取资产分类名称
            LambdaQueryWrapper<AssetCategory> getCategoryNameById = new LambdaQueryWrapper<>();
            getCategoryNameById.in(AssetCategory::getId, param.getItemList().stream().map(RequisitionOrderCreateParam.itemList::getCategoryId).toList());
            List<AssetCategory> assetCategories = assetCategoryMapper.selectList(getCategoryNameById);
            Map<Long,String> categoryMap = assetCategories.stream().collect(Collectors.toMap(AssetCategory::getId, AssetCategory::getName));

            for (RequisitionOrderCreateParam.itemList list : param.getItemList()) {
                RequisitionOrderItem requisitionOrderItem = new RequisitionOrderItem();
                requisitionOrderItem.setOrderId(requisitionOrder1.getId());
                requisitionOrderItem.setCategoryId(list.getCategoryId());
                requisitionOrderItem.setCategoryName(categoryMap.get(list.getCategoryId()));
                requisitionOrderItem.setQuantity(list.getQuantity());
                requisitionOrderItem.setCreatedAt(LocalDateTime.now());
                requisitionOrderItemMapper.insert(requisitionOrderItem);
            }

            //新建审批表
            ApprovalRecord approvalRecord = new ApprovalRecord();
            approvalRecord.setApprovalType("APPLY");
            approvalRecord.setTargetType("requisition_order");
            approvalRecord.setTargetId(requisitionOrder1.getId());
            approvalRecord.setApplicantId(context.getId());
            User currentUser = userMapper.selectById(context.getId());
            Department department = departmentMapper.selectById(currentUser.getDepartmentId());
            approvalRecord.setApproverId(department != null ? department.getManagerUserId() : null);
            approvalRecord.setApprovalStatus("PENDING");
            approvalRecord.setCreatedAt(LocalDateTime.now());
            approvalRecord.setUpdatedAt(LocalDateTime.now());
            approvalRecordMapper.insert(approvalRecord);







            RequisitionOrderCreateVO requisitionOrderCreateVO = new RequisitionOrderCreateVO();
            requisitionOrderCreateVO.setId(requisitionOrder1.getId());
            requisitionOrderCreateVO.setOrderNo(requisitionOrder1.getOrderNo());
            requisitionOrderCreateVO.setApprovalStatus(requisitionOrder1.getApprovalStatus());
            requisitionOrderCreateVO.setCreateTime(requisitionOrder1.getCreatedAt());
            return requisitionOrderCreateVO;


        }

        throw new CommonException(500, "申领单创建失败");
    }

    @Override
    public RequisitionOrderPageVO queryPage(RequisitionOrderPageParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());

        List<RequisitionOrderPageRecord>  list  = requisitionOrderMapper.getPage(param);
        Page<RequisitionOrderPageRecord> pageInfo = (Page<RequisitionOrderPageRecord>) list;
        RequisitionOrderPageVO requisitionOrderPageVO = new RequisitionOrderPageVO();
        requisitionOrderPageVO.setTotal(pageInfo.getTotal());
        requisitionOrderPageVO.setRecords(pageInfo);
        requisitionOrderPageVO.setPage(param.getPage());
        requisitionOrderPageVO.setSize(param.getSize());
        return requisitionOrderPageVO;
    }

    /*
    * 申领单详情
    * */
    @Override
    public RequisitionOrderDetailVO detail(RequisitionOrderDetailParam param) {
        //查询申领单主表
        RequisitionOrder requisitionOrder = requisitionOrderMapper.selectById(param.getId());
        if (requisitionOrder == null) {
            throw new CommonException(404, "申领单不存在");
        }
        RequisitionOrderDetailVO requisitionOrderDetailVO = new RequisitionOrderDetailVO();
        requisitionOrderDetailVO.setId(requisitionOrder.getId());
        requisitionOrderDetailVO.setOrderNo(requisitionOrder.getOrderNo());
        requisitionOrderDetailVO.setApplicantId(requisitionOrder.getApplicantId());
        requisitionOrderDetailVO.setApplicantName(requisitionOrder.getApplicantName());
        requisitionOrderDetailVO.setReason(requisitionOrder.getReason());
        requisitionOrderDetailVO.setApprovalStatus(requisitionOrder.getApprovalStatus());
        //查询申领单明细表
        LambdaQueryWrapper<RequisitionOrderItem> getItemListByOrderId = new LambdaQueryWrapper<>();
        getItemListByOrderId.eq(RequisitionOrderItem::getOrderId, param.getId());
        List<RequisitionOrderItem> requisitionOrderItems = requisitionOrderItemMapper.selectList(getItemListByOrderId);
        if (requisitionOrderItems != null &&  !requisitionOrderItems.isEmpty()){
            List<RequisitionOrderDetailItemVO> itemVOList = requisitionOrderItems.stream().map(item -> {
                RequisitionOrderDetailItemVO itemVO = new RequisitionOrderDetailItemVO();
                itemVO.setId(item.getId());
                itemVO.setCategoryId(item.getCategoryId());
                itemVO.setCategoryName(item.getCategoryName());
                itemVO.setQuantity(item.getQuantity());
                return itemVO;
            }).collect(Collectors.toList());
            requisitionOrderDetailVO.setItemList(itemVOList);



        }

        return requisitionOrderDetailVO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RequisitionOrderOutBoundVO comfirmOutbound(RequisitionOrderOutBoundParam param) {
        RequisitionOrder requisitionOrder =requisitionOrderMapper.selectById(param.getId());
        if (requisitionOrder == null) {
            throw new CommonException(404, "申领单不存在");
        }
        //出库确认
        if("APPROVED".equals(requisitionOrder.getApprovalStatus())){
            //更新申领单主表
           LoginUserContext context = LoginUserInfoUtile.get();
           requisitionOrder.setOutboundStaffName(context.getRealName());
           requisitionOrder.setOutboundAt(LocalDateTime.now());
           requisitionOrder.setConfirmRemark(param.getConfirmRemark());
           requisitionOrder.setOrderStatus("OUTBOUND");
           LambdaQueryWrapper<RequisitionOrder> queryWrapper = new LambdaQueryWrapper<>();
           queryWrapper.eq(RequisitionOrder::getId,param.getId());
           requisitionOrderMapper.update(requisitionOrder, queryWrapper);

           //更新申领的资产表及申领单资产明细表

            param.getItemList().stream().map(item -> {
                Asset asset = assetMapper.selectById(item.getAssetId());
                // 检查是否在转移审批中
                LambdaQueryWrapper<TransferOrderItem> transferCheck = new LambdaQueryWrapper<>();
                transferCheck.eq(TransferOrderItem::getAssetId, item.getAssetId());
                TransferOrderItem transferItem = transferOrderItemMapper.selectOne(transferCheck);
                if (transferItem != null) {
                    TransferOrder transferOrder = transferOrderMapper.selectById(transferItem.getOrderId());
                    if ("PENDING".equals(transferOrder.getApprovalStatus())) {
                        throw new CommonException(400, "资产 " + asset.getName() + " 正在转移审批中，无法出库");
                    }
                }
                // 检查是否在报废审批中
                LambdaQueryWrapper<ScrapOrderItem> scrapCheck = new LambdaQueryWrapper<>();
                scrapCheck.eq(ScrapOrderItem::getAssetId, item.getAssetId());
                ScrapOrderItem scrapItem = scrapOrderItemMapper.selectOne(scrapCheck);
                if (scrapItem != null) {
                    ScrapOrder scrapOrder = scrapOrderMapper.selectById(scrapItem.getOrderId());
                    if ("PENDING".equals(scrapOrder.getApprovalStatus())) {
                        throw new CommonException(400, "资产 " + asset.getName() + " 正在报废审批中，无法出库");
                    }
                }
                // 检查是否在归还审批中
                LambdaQueryWrapper<ReturnOrderItem> returnCheck = new LambdaQueryWrapper<>();
                returnCheck.eq(ReturnOrderItem::getAssetId, item.getAssetId());
                ReturnOrderItem returnItem = returnOrderItemMapper.selectOne(returnCheck);
                if (returnItem != null) {
                    ReturnOrder returnOrder = returnOrderMapper.selectById(returnItem.getOrderId());
                    if ("PENDING".equals(returnOrder.getApprovalStatus())) {
                        throw new CommonException(400, "资产 " + asset.getName() + " 正在归还审批中，无法出库");
                    }
                }
                //查询出申请人的信息
                User user = userMapper.selectById(requisitionOrder.getApplicantId());
                if("STOCK".equals(asset.getStatus()) && asset.getIsDeleted() == 0){
                    asset.setCurrentUserId(user.getId());
                    asset.setStatus("ASSIGNED");
                    //更新资产信息
                    assetMapper.update(asset,new LambdaQueryWrapper<Asset>().eq(Asset::getId,item.getAssetId()));

                    //更新资产明细表
                    RequisitionOrderItem requisitionOrderItem = requisitionOrderItemMapper.selectById(item.getItemId());
                    requisitionOrderItem.setAssetCode(asset.getAssetCode());
                    requisitionOrderItem.setAssetName(asset.getName());
                    requisitionOrderItem.setBrandModel(asset.getBrandModel());
                    LambdaQueryWrapper<AssetLedger> queryLedgerNoByAsserId = new LambdaQueryWrapper<>();
                    queryLedgerNoByAsserId.eq(AssetLedger::getAssetId,asset.getId());
                    AssetLedger assetLedger = assetLedgerMapper.selectOne(queryLedgerNoByAsserId);
                    requisitionOrderItem.setLedgerNo(assetLedger.getLedgerNo());
                    requisitionOrderItem.setNetValueAtApply(assetLedger.getNetValue());
                    requisitionOrderItemMapper.update(requisitionOrderItem,new LambdaQueryWrapper<RequisitionOrderItem>().eq(RequisitionOrderItem::getId,item.getItemId()));


                }
                return null;
            }).collect(Collectors.toList());
        }
        LambdaQueryWrapper<RequisitionOrder> queryOrderStatus = new LambdaQueryWrapper<>();
        queryOrderStatus.eq(RequisitionOrder::getId,param.getId());

        RequisitionOrderOutBoundVO requisitionOrderOutBoundVO = new RequisitionOrderOutBoundVO();
        requisitionOrderOutBoundVO.setSuccess(true);
        RequisitionOrder latest = requisitionOrderMapper.selectById(param.getId());
        requisitionOrderOutBoundVO.setOrderStatus(latest.getOrderStatus());
        return requisitionOrderOutBoundVO;
    }


}
