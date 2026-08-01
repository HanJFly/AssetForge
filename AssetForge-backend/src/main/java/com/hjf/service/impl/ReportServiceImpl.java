package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.entity.AssetCategory;
import com.hjf.entity.AssetLedger;
import com.hjf.entity.AssetLedgerSnapshot;
import com.hjf.mapper.AssetCategoryMapper;
import com.hjf.mapper.AssetLedgerSnapshotMapper;
import com.hjf.mapper.ReportMapper;
import com.hjf.param.ReportAssetDetailParam;
import com.hjf.param.ReportLedgerSnapshotPageParam;
import com.hjf.param.ReportmonthlySummaryParam;
import com.hjf.service.IReportService;
import com.hjf.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements IReportService {
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private AssetLedgerSnapshotMapper assetLedgerSnapshotMapper;
    @Autowired
    private AssetCategoryMapper assetCategoryMapper;
    @Override
    public ReportAssetDetailVO detail(ReportAssetDetailParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        //查询
        List<ReportAssetDetailPageRecord> list = reportMapper.detail(param);
        Page<ReportAssetDetailPageRecord> pageInfo = (Page<ReportAssetDetailPageRecord>) list;
        ReportAssetDetailVO vo = new ReportAssetDetailVO();
        vo.setTotal(pageInfo.getTotal());
        vo.setPage(pageInfo.getPageNum());
        vo.setRecords(pageInfo);
        vo.setPageSize(pageInfo.getPageSize());
        return vo;

    }

    @Override
    public ReportLedgerSnapshotPageVO ledgerSnapshotPage(ReportLedgerSnapshotPageParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        //查询
        List<ReportLedgerSnapshotPageRecord> list = reportMapper.ledgerSnapshotPage(param);
        Page<ReportLedgerSnapshotPageRecord> pageInfo = (Page<ReportLedgerSnapshotPageRecord>) list;
        ReportLedgerSnapshotPageVO vo = new ReportLedgerSnapshotPageVO();
        vo.setTotal(pageInfo.getTotal());
        vo.setPage(pageInfo.getPageNum());
        vo.setRecords(pageInfo);
        vo.setSize(pageInfo.getPageSize());
        return vo;


    }

    @Override
    public ReportmonthlySummaryVO monthlySummary(ReportmonthlySummaryParam param) {
        //查找指定月份的所有快照
        LambdaQueryWrapper<AssetLedgerSnapshot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetLedgerSnapshot::getSnapshotMonth, param.getSnapshotMonth());
        List<AssetLedgerSnapshot> list = assetLedgerSnapshotMapper.selectList(queryWrapper);
        //计算汇总
        ReportmonthlySummaryVO vo = new ReportmonthlySummaryVO();
        vo.setSnapshotMonth(param.getSnapshotMonth());
        vo.setAssetCount((long) list.size());
        vo.setOriginalAmountTotal(
                list.stream().map(AssetLedgerSnapshot::getOriginalValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setMonthlyDepreciationTotal(
                list.stream().map(AssetLedgerSnapshot::getMonthlyDepreciation)
                        .reduce(BigDecimal.ZERO,BigDecimal::add));
        vo.setAccumulatedDepreciationTotal(
                list.stream().map(AssetLedgerSnapshot::getAccumulatedDepreciation)
                        .reduce(BigDecimal.ZERO,BigDecimal::add));
        vo.setNetAmountTotal(
                list.stream().map(AssetLedgerSnapshot::getNetValue)
                        .reduce(BigDecimal.ZERO,BigDecimal::add));


        //按分类分组
        //给查到的list集合转成一个map，key是分类名称，value是分类下的所有快照
       Map<String,List<AssetLedgerSnapshot>> groupByCategory = list.stream().collect(Collectors.groupingBy(AssetLedgerSnapshot::getCategoryName));
       List<CategorySummaryList> categorySummary = new ArrayList<>();

        // 获取所有分类名称（去重）
        Set<String> categoryNames = groupByCategory.keySet();
        LambdaQueryWrapper<AssetCategory> queryWrapperByCategoryName = new LambdaQueryWrapper<>();
        queryWrapperByCategoryName.in(AssetCategory::getName,categoryNames);
        List<AssetCategory> assetCategories = assetCategoryMapper.selectList(queryWrapperByCategoryName);
        Map<String,Long> categoryIdMap = assetCategories.stream().collect(Collectors.toMap(AssetCategory::getName, AssetCategory::getId));

       //遍历这个map，将每个分类下的所有快照转成一个CategorySummaryList
       groupByCategory.forEach((categoryName,assetLedgerSnapshots)->{
          CategorySummaryList categorySummaryList = new CategorySummaryList();
          categorySummaryList.setCategoryId(categoryIdMap.get(categoryName));
          categorySummaryList.setCategoryName(categoryName);
          categorySummaryList.setAssetCount((long) assetLedgerSnapshots.size());
          categorySummaryList.setOriginalAmountTotal(assetLedgerSnapshots.stream().map(AssetLedgerSnapshot::getOriginalValue)
                  .reduce(BigDecimal.ZERO,BigDecimal::add));
          categorySummaryList.setNetAmountTotal(assetLedgerSnapshots.stream().map(AssetLedgerSnapshot::getNetValue)
                  .reduce(BigDecimal.ZERO,BigDecimal::add));
          categorySummary.add(categorySummaryList);
       });
       vo.setCategorySummary(categorySummary);
       return vo;


    }
}
