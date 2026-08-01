package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.entity.*;
import com.hjf.mapper.*;
import com.hjf.param.DepreciationRunLogExecuteParam;
import com.hjf.param.DepreciationRunLogPageParam;
import com.hjf.service.IDepreciationRunLogService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.DepreciationRunLogExecuteVO;
import com.hjf.vo.DepreciationRunLogPageRecord;
import com.hjf.vo.DepreciationRunLogPageVO;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 月度折旧执行记录表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class DepreciationRunLogServiceImpl extends ServiceImpl<DepreciationRunLogMapper, DepreciationRunLog> implements IDepreciationRunLogService {

    @Autowired
    private DepreciationRunLogMapper depreciationRunLogMapper;
    @Autowired
    private AssetMapper assetMapper;
    @Autowired
    private AssetCategoryMapper assetCategoryMapper;
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;
    @Autowired
    private AssetLedgerSnapshotMapper assetLedgerSnapshotMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepreciationRunLogExecuteVO execute(DepreciationRunLogExecuteParam param) {
        LambdaQueryWrapper<DepreciationRunLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DepreciationRunLog::getRunMonth, param.getRunMonth());
        DepreciationRunLog log = depreciationRunLogMapper.selectOne(queryWrapper);
        if (log != null){
            throw new CommonException(400, "该月度折旧执行记录已存在");
        }
        DepreciationRunLog newLog = new DepreciationRunLog();
        newLog.setStatus("RUNNING");
        newLog.setStartedAt(LocalDateTime.now());
        newLog.setCreatedAt(LocalDateTime.now());
        List<AssetLedger> assetLedgers = assetLedgerMapper.selectList(null);
        List<Asset> assets = assetMapper.selectList(null);
        List<AssetCategory> assetCategories = assetCategoryMapper.selectList(null);
        Map<Long,Long> assetIdToCategoryId = assets.stream().filter(a -> a.getCategoryId() != null).collect(Collectors.toMap(Asset::getId, Asset::getCategoryId));
        Map<Long,String> categoryIdToName = assetCategories.stream().collect(Collectors.toMap(AssetCategory::getId, AssetCategory::getName));
        //通过资产Id获取资产状态
        Map<Long, String> assetIdToStatus = assets.stream().filter(a -> a.getStatus() != null).collect(Collectors.toMap(Asset::getId, Asset::getStatus));
        //通过资产Id获取资产分类名称
        Map<Long, String> assetIdToCategoryName = assetIdToCategoryId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> categoryIdToName.getOrDefault(entry.getValue(), "未知分类") // 找不到时给个默认值
                ));
        Map<Long, String> assetIdToAssetCode = assets.stream().collect(Collectors.toMap(Asset::getId, Asset::getAssetCode));
        Map<Long, String> assetIdToName = assets.stream().collect(Collectors.toMap(Asset::getId, Asset::getName));
        List<User> users = userMapper.selectList(null);
        Map<Long, String> userIdToName = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));
        Map<Long,Long> assetIdToUserId = assets.stream().filter(a -> a.getCurrentUserId() != null).collect(Collectors.toMap(Asset::getId, Asset::getCurrentUserId));
        //通过资产Id获取资产用户名称
        Map<Long, String> assetIdToUserName = assetIdToUserId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userIdToName.getOrDefault(entry.getValue(), "未知用户")
                ));
        Map<Long, String> userIdToEmployeeNo = users.stream().collect(Collectors.toMap(User::getId, User::getEmployeeNo));
        //通过资产Id获取资产员工编号
        Map<Long, String> assetIdToEmployeeNo = assetIdToUserId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userIdToEmployeeNo.getOrDefault(entry.getValue(), "未知员工")
                ));
        Map<Long, String> userIdToDepartmentName = users.stream().collect(Collectors.toMap(User::getId, User::getDepartmentName));
        //通过资产Id获取资产部门名称
        Map<Long, String> assetIdToDepartmentName = assetIdToUserId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userIdToDepartmentName.getOrDefault(entry.getValue(), "未知部门")
                ));
        //通过资产Id获取资产是否删除
        Map<Long, Byte> assetIdToIsDeleted = assets.stream().filter(a -> a.getIsDeleted() != null).collect(Collectors.toMap(Asset::getId, Asset::getIsDeleted));
        Integer processedCount = 0;
        Integer skippedCount = 0;
        BigDecimal totalMonthlyDepreciation = BigDecimal.ZERO;
        for (AssetLedger assetLedger : assetLedgers) {
            if(assetIdToIsDeleted.get(assetLedger.getAssetId()) == 1 || "LOST".equals(assetIdToStatus.get(assetLedger.getAssetId()))
                    || "SCRAPPED".equals(assetIdToStatus.get(assetLedger.getAssetId()))){
                skippedCount ++;
                continue;

            }
            boolean shouldSkipDepreciation = false;
            //判断该资产是否是这个月刚入库的
            String entryDate = assetLedger.getEntryDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if(entryDate.equals(param.getRunMonth())){
                assetLedger.setMonthsUsed(0);
                assetLedger.setAccumulatedDepreciation(BigDecimal.ZERO);
                assetLedger.setNetValue(assetLedger.getOriginalValue());
                skippedCount ++;
                shouldSkipDepreciation = true;
            }
            if (!shouldSkipDepreciation && assetLedger.getMonthsUsed() >= assetLedger.getStandardLifeMonths()){
                //判断使用月数是否大于等于标准使用年限
                assetLedger.setMonthsUsed(assetLedger.getMonthsUsed() + 1);
                BigDecimal residualValue = assetLedger.getOriginalValue()
                        .multiply(assetLedger.getResidualRate());
                assetLedger.setNetValue(residualValue);
                shouldSkipDepreciation = true;
                skippedCount++;
            }
            if (!shouldSkipDepreciation){

                assetLedger.setMonthsUsed(assetLedger.getMonthsUsed() + 1);
                assetLedger.setAccumulatedDepreciation(assetLedger.getAccumulatedDepreciation().add(assetLedger.getMonthlyDepreciation()));
                assetLedger.setNetValue(assetLedger.getNetValue().subtract(assetLedger.getMonthlyDepreciation()));
                totalMonthlyDepreciation = totalMonthlyDepreciation.add(assetLedger.getMonthlyDepreciation());
                processedCount++;
            }
            //更新台账
            assetLedgerMapper.updateById(assetLedger);
            //创建台账快照
            AssetLedgerSnapshot assetLedgerSnapshot = new AssetLedgerSnapshot();
            assetLedgerSnapshot.setAssetId(assetLedger.getAssetId());
            assetLedgerSnapshot.setLedgerId(assetLedger.getId());
            assetLedgerSnapshot.setSnapshotMonth(param.getRunMonth());
            assetLedgerSnapshot.setAssetCode(assetIdToAssetCode.get(assetLedger.getAssetId()));
            assetLedgerSnapshot.setAssetName(assetIdToName.get(assetLedger.getAssetId()));
            assetLedgerSnapshot.setDepartmentName(assetLedger.getDepartmentName());
            assetLedgerSnapshot.setEntryDate(assetLedger.getEntryDate());
            assetLedgerSnapshot.setOriginalValue(assetLedger.getOriginalValue());
            assetLedgerSnapshot.setStandardLifeMonths(assetLedger.getStandardLifeMonths());
            assetLedgerSnapshot.setMonthlyDepreciation(assetLedger.getMonthlyDepreciation());
            assetLedgerSnapshot.setCategoryName(assetIdToCategoryName.get(assetLedger.getAssetId()));
            assetLedgerSnapshot.setAccumulatedDepreciation(assetLedger.getAccumulatedDepreciation());
            assetLedgerSnapshot.setNetValue(assetLedger.getNetValue());
            assetLedgerSnapshot.setMonthsUsed(assetLedger.getMonthsUsed());
            assetLedgerSnapshot.setCurrentUserName(assetIdToUserName.get(assetLedger.getAssetId()));
            assetLedgerSnapshot.setCurrentUserEmployeeNo(assetIdToEmployeeNo.get(assetLedger.getAssetId()));
            assetLedgerSnapshot.setCurrentUserDepartment(assetIdToDepartmentName.get(assetLedger.getAssetId()));
            assetLedgerSnapshot.setAssetStatus(assetIdToStatus.get(assetLedger.getAssetId()));
            assetLedgerSnapshot.setCreatedAt(LocalDateTime.now());
            assetLedgerSnapshotMapper.insert(assetLedgerSnapshot);

        }
        newLog.setStatus("SUCCESS");
        newLog.setRunMonth(param.getRunMonth());
        newLog.setCompletedAt(LocalDateTime.now());
        newLog.setProcessedCount(processedCount);
        newLog.setSkippedCount(skippedCount);
        newLog.setTotalMonthlyDepreciation(totalMonthlyDepreciation);
        depreciationRunLogMapper.insert(newLog);

        DepreciationRunLogExecuteVO vo = new DepreciationRunLogExecuteVO();
        vo.setId(newLog.getId());
        vo.setStatus(newLog.getStatus());
        vo.setRunMonth(newLog.getRunMonth());
        vo.setProcessedCount(newLog.getProcessedCount());

        return vo;

    }

    @Override
    public DepreciationRunLogPageVO qurypage(DepreciationRunLogPageParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        List<DepreciationRunLogPageRecord> list = depreciationRunLogMapper.queryPage(param);
        Page<DepreciationRunLogPageRecord> pageInfo = (Page<DepreciationRunLogPageRecord>)list;
        DepreciationRunLogPageVO vo = new DepreciationRunLogPageVO();
        vo.setTotal(pageInfo.getTotal());
        vo.setRecords(pageInfo);
        vo.setPage(pageInfo.getPageNum());
        vo.setSize(pageInfo.getPageSize());
        return vo;

    }
}
