package com.hjf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.*;
import com.hjf.mapper.*;
import com.hjf.param.*;
import com.hjf.service.IInventoryTaskService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 盘点任务表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class InventoryTaskServiceImpl extends ServiceImpl<InventoryTaskMapper, InventoryTask> implements IInventoryTaskService {
   @Autowired
   private InventoryTaskMapper inventoryTaskMapper;
   @Autowired
   private InventoryDetailMapper inventoryDetailMapper;
   @Autowired
   private AssetMapper assetMapper;
   @Autowired
   private LossOrderMapper lossOrderMapper;
   @Autowired
   private AssetCategoryMapper assetCategoryMapper;
   @Autowired
   private AssetLedgerMapper assetLedgerMapper;
   @Autowired
   private UserMapper userMapper;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryTaskCreateVO create(InventoryTaskCreateParam param) {

        LoginUserContext context = LoginUserInfoUtile.get();
        InventoryTask inventoryTask = new InventoryTask();
        LambdaQueryWrapper<InventoryTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryTask::getTaskName, param.getTaskName());
        queryWrapper.eq(InventoryTask::getIsDeleted,0);
        if (inventoryTaskMapper.selectOne(queryWrapper) != null){
            throw new CommonException(400, "任务名已存在");
        }
        inventoryTask.setTaskName(param.getTaskName());
        inventoryTask.setScopeType(param.getScopeType());
        inventoryTask.setDeadLine(param.getDeadLine());
        //责任人为库管员，前端传过来的
        inventoryTask.setResponsibleUserId(param.getResponsibleUserId());
        inventoryTask.setCreatedAt(LocalDateTime.now());
        inventoryTask.setUpdatedAt(LocalDateTime.now());
        inventoryTask.setStatus("PENDING");
        inventoryTask.setScopeValue(JSONUtil.toJsonStr(param.getScopeValue()));
        inventoryTask.setAssetStatusFilter(JSONUtil.toJsonStr(param.getAssetStatusFilter()));
        //判断scopeType的类型
        switch (param.getScopeType()){
            case "ALL":
                LambdaQueryWrapper<Asset> queryWrapperAll = new LambdaQueryWrapper<>();
                queryWrapperAll.eq(Asset::getIsDeleted,0);
                if(param.getAssetStatusFilter() != null && !param.getAssetStatusFilter().isEmpty()){
                    queryWrapperAll.in(Asset::getStatus, param.getAssetStatusFilter());
                }
                List<Asset> assetList = assetMapper.selectList(queryWrapperAll);
                if (assetList == null || assetList.isEmpty()){
                    throw new CommonException(404, "没有资产");
                }
                //保存盘点任务

                inventoryTaskMapper.insert(inventoryTask);
                //更新盘点明细
                for (Asset asset : assetList) {
                    InventoryDetail detail = new InventoryDetail();
                    detail.setTaskId(inventoryTask.getId());
                    detail.setAssetId(asset.getId());
                    detail.setSystemUserId(asset.getCurrentUserId());
                    detail.setCreatedAt(LocalDateTime.now());
                    detail.setUpdatedAt(LocalDateTime.now());
                    detail.setCheckerId(context.getId());  // 当前登录用户，即创建任务的人
                    inventoryDetailMapper.insert(detail);

                }
                InventoryTaskCreateVO vo = new InventoryTaskCreateVO();
                vo.setId(inventoryTask.getId());
                vo.setStatus(inventoryTask.getStatus());
                vo.setDetailCount(assetList.size());
                vo.setCreatedAt(inventoryTask.getCreatedAt());
                return vo;




            case "DEPARTMENT":
                LambdaQueryWrapper<Asset> queryWrapperDepartment = new LambdaQueryWrapper<>();
                queryWrapperDepartment.eq(Asset::getIsDeleted,0);
                //按资产状态
                if (param.getAssetStatusFilter() != null && !param.getAssetStatusFilter().isEmpty()){
                    queryWrapperDepartment.in(Asset::getStatus, param.getAssetStatusFilter());
                }
                // 按部门ID过滤
                if (param.getScopeValue() != null && !param.getScopeValue().isEmpty()) {
                    queryWrapperDepartment.in(Asset::getDepartmentId, param.getScopeValue());
                }

                //查询符合条件的资产
                List<Asset> assetListDepartment = assetMapper.selectList(queryWrapperDepartment);
                if (assetListDepartment == null || assetListDepartment.isEmpty()){
                    throw new CommonException(404, "没有资产");
                }
                //保存盘点任务
                inventoryTaskMapper.insert(inventoryTask);
                //更新盘点明细
                for (Asset asset : assetListDepartment) {
                    InventoryDetail detail = new InventoryDetail();
                    detail.setTaskId(inventoryTask.getId());
                    detail.setAssetId(asset.getId());
                    detail.setSystemUserId(asset.getCurrentUserId());
                    detail.setCreatedAt(LocalDateTime.now());
                    detail.setUpdatedAt(LocalDateTime.now());
                    detail.setCheckerId(context.getId());  // 当前登录用户，即创建任务的人
                    inventoryDetailMapper.insert(detail);

                }
                InventoryTaskCreateVO voD = new InventoryTaskCreateVO();
                voD.setId(inventoryTask.getId());
                voD.setStatus(inventoryTask.getStatus());
                voD.setDetailCount(assetListDepartment.size());
                voD.setCreatedAt(inventoryTask.getCreatedAt());
                return voD;



            case "CATEGORY":
                LambdaQueryWrapper<Asset> queryWrapperCategory = new LambdaQueryWrapper<>();
                queryWrapperCategory.eq(Asset::getIsDeleted,0);
                if (param.getAssetStatusFilter() != null && !param.getAssetStatusFilter().isEmpty()){
                    queryWrapperCategory.in(Asset::getStatus, param.getAssetStatusFilter());
                }
                //按资产分类过滤
                if (param.getScopeValue() != null && !param.getScopeValue().isEmpty()) {
                    queryWrapperCategory.in(Asset::getCategoryId, param.getScopeValue());
                }
                List<Asset> assetListCategory = assetMapper.selectList(queryWrapperCategory);
                if (assetListCategory == null || assetListCategory.isEmpty()){
                    throw new CommonException(404, "没有资产");
                }
                //保存盘点任务
                inventoryTaskMapper.insert(inventoryTask);
                //更新盘点明细
                for (Asset asset : assetListCategory) {
                    InventoryDetail detail = new InventoryDetail();
                    detail.setTaskId(inventoryTask.getId());
                    detail.setAssetId(asset.getId());
                    detail.setSystemUserId(asset.getCurrentUserId());
                    detail.setCreatedAt(LocalDateTime.now());
                    detail.setUpdatedAt(LocalDateTime.now());
                    detail.setCheckerId(context.getId());  // 当前登录用户，即创建任务的人
                    inventoryDetailMapper.insert(detail);
                }
                InventoryTaskCreateVO voC = new InventoryTaskCreateVO();
                voC.setId(inventoryTask.getId());
                voC.setStatus(inventoryTask.getStatus());
                voC.setDetailCount(assetListCategory.size());
                voC.setCreatedAt(inventoryTask.getCreatedAt());
                return voC;



            default:
                throw new CommonException(400, "未知的盘点范围类型: " + param.getScopeType());
        }

    }

    @Override
    public InventoryTaskPageVO queryPage(InventoryTaskPageParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());

        List<InventoryTaskPageRecord> records = inventoryTaskMapper.queryPage(param);
        Page<InventoryTaskPageRecord> pageInfo = (Page<InventoryTaskPageRecord>) records;
        InventoryTaskPageVO vo = new InventoryTaskPageVO();
        vo.setTotal(pageInfo.getTotal());
        vo.setPage(pageInfo.getPageNum());
        vo.setPageSize(pageInfo.getPageSize());
        vo.setRecords(pageInfo);
        return vo;

    }

    @Override
    public InventoryTaskDeatilVO detail(InventoryTaskDeatilParam param) {
        InventoryTask task = inventoryTaskMapper.selectById(param.getId());
        if(task == null){
            throw new CommonException(404, "盘点任务不存在");
        }
        InventoryTaskDeatilVO vo = new InventoryTaskDeatilVO();
        BeanUtil.copyProperties(task, vo);
        vo.setScopeValue(JSONUtil.toList(task.getScopeValue(), Long.class));
        vo.setAssetStatusFilter(JSONUtil.toList(task.getAssetStatusFilter(), String.class));

        return vo;

    }

    @Override
    public InventoryTaskReportVO report(InventoryTaskReportParam param) {
        InventoryTaskReportVO vo = new InventoryTaskReportVO();
        vo =  inventoryTaskMapper.queryCount(param);
        return vo;


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryTaskConclusionVO updateConclusion(InventoryTaskConclusionParam param) {
        //判断盘点任务是否存在
        InventoryTask task = inventoryTaskMapper.selectById(param.getId());
        if(task == null){
            throw new CommonException(404, "盘点任务不存在");
        }
        if ("COMPLETED".equals(task.getStatus())) {
            throw new CommonException(400, "盘点任务已完结，不能重复结案");
        }
        if ("PENDING".equals(task.getStatus())) {
            throw new CommonException(400, "盘点任务尚未开始，无法结案");
        }
        task.setConclusion(param.getConclusion());
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        inventoryTaskMapper.updateById(task);
        //查询所有 LOSS 的明细，自动生成盘亏单 + 更新资产状态
        LambdaQueryWrapper<InventoryDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryDetail::getTaskId, param.getId());
        queryWrapper.eq(InventoryDetail::getResult, "LOSS");
        List<InventoryDetail> inventoryDetails = inventoryDetailMapper.selectList(queryWrapper);
        if(inventoryDetails != null && !inventoryDetails.isEmpty()){
            for (InventoryDetail detail : inventoryDetails) {
                Asset asset = assetMapper.selectById(detail.getAssetId());
                if(asset != null){
                    asset.setStatus("LOSS");
                    asset.setUpdatedAt(LocalDateTime.now());
                    assetMapper.updateById(asset);
                }
                //创建盘亏单
                LossOrder lossOrder = new LossOrder();
                lossOrder.setInventoryTaskId(task.getId());
                lossOrder.setInventoryDetailId(detail.getId());
                //设置单号
                lossOrder.setOrderNo(generateOrderNo());
                lossOrder.setAssetId(detail.getAssetId());
                lossOrder.setAssetCode(asset.getAssetCode());
                lossOrder.setAssetName(asset.getName());
                AssetCategory assetCategory = assetCategoryMapper.selectById(asset.getCategoryId());
                lossOrder.setCategoryName(assetCategory.getName());
                AssetLedger assetLedger = assetLedgerMapper.selectOne(new LambdaQueryWrapper<AssetLedger>().eq(AssetLedger::getAssetId,asset.getId()));
                lossOrder.setLedgerNo(assetLedger.getLedgerNo());
                lossOrder.setRemark(detail.getRemark());
                lossOrder.setResponsibleUserId(asset.getCurrentUserId());
                User user = userMapper.selectById(asset.getCurrentUserId());
                lossOrder.setResponsibleUserName(user.getRealName());
                lossOrder.setResponsibleUserEmployeeNo(user.getEmployeeNo());
                lossOrder.setResponsibleDepartmentId(user.getDepartmentId());
                lossOrder.setResponsibleDepartmentName(user.getDepartmentName());
                lossOrder.setSuggestedCompensation(assetLedger.getNetValue());
                lossOrder.setCompensationStatus("PENDING_COMPENSATION");
                LoginUserContext context = LoginUserInfoUtile.get();
                lossOrder.setHandlerId(context.getId());
                lossOrder.setCreatedAt(LocalDateTime.now());
                lossOrder.setUpdatedAt(LocalDateTime.now());
                lossOrderMapper.insert(lossOrder);

            }

        }

        InventoryTaskConclusionVO voC = new InventoryTaskConclusionVO();
        voC.setSuccess(true);
        return voC;




    }

    //生成单号方法
    private String generateOrderNo() {
        String orderOne = "LOSS";
        String orderSecond = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = orderOne + "-" + orderSecond+ "-" ;
        LambdaQueryWrapper<LossOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(LossOrder::getOrderNo, prefix);
        queryWrapper.orderByDesc(LossOrder::getOrderNo);
        queryWrapper.last("limit 1");
        LossOrder lossOrder = lossOrderMapper.selectOne(queryWrapper);
        if (lossOrder == null) {
            return prefix + "000001";
        }

        String orderNo = lossOrder.getOrderNo();
        String substring = orderNo.substring(orderNo.lastIndexOf("-") + 1);
        int num = Integer.parseInt(substring) + 1;
        return prefix + String.format("%06d", num);

    }
}
