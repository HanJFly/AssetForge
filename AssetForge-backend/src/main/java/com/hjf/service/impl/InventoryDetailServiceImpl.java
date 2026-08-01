package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.entity.InventoryDetail;
import com.hjf.entity.InventoryTask;
import com.hjf.mapper.InventoryDetailMapper;
import com.hjf.mapper.InventoryTaskMapper;
import com.hjf.param.InventoryDetailPageParam;
import com.hjf.param.InventoryDetailSubmitList;
import com.hjf.param.InventoryDetailSubmitParam;
import com.hjf.service.IInventoryDetailService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.InventoryDetailPageRecord;
import com.hjf.vo.InventoryDetailPageVO;
import com.hjf.vo.InventoryDetailSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘点明细表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class InventoryDetailServiceImpl extends ServiceImpl<InventoryDetailMapper, InventoryDetail> implements IInventoryDetailService {

    @Autowired
    private InventoryDetailMapper inventoryDetailMapper;
    @Autowired
    private InventoryTaskMapper inventoryTaskMapper;
    @Override
    public InventoryDetailPageVO queryPage(InventoryDetailPageParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        List<InventoryDetailPageRecord> list =  inventoryDetailMapper.queryPage(param);
        Page<InventoryDetailPageRecord> pageInfo = (Page<InventoryDetailPageRecord>) list;
        InventoryDetailPageVO vo = new InventoryDetailPageVO();
        vo.setRecords(pageInfo);
        vo.setTotal(pageInfo.getTotal());
        vo.setPage(pageInfo.getPageNum());
        vo.setPageSize(pageInfo.getPageSize());
        return vo;




    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryDetailSubmitVO submit(InventoryDetailSubmitParam param) {
        //判断盘点任务是否不存在或已经提交
        InventoryTask inventoryTask = inventoryTaskMapper.selectById(param.getTaskId());
        if (inventoryTask == null){
            throw new CommonException(404, "盘点任务不存在");
        }
        if (inventoryTask.getStatus().equals("COMPLETED")){
            throw new CommonException(400, "盘点任务已提交");
        }
        LambdaQueryWrapper<InventoryDetail> queryWrapperDetail = new LambdaQueryWrapper<>();
        queryWrapperDetail.eq(InventoryDetail::getTaskId, param.getTaskId());
        List<InventoryDetail> inventoryDetailList = inventoryDetailMapper.selectList(queryWrapperDetail);
        Map<Long , InventoryDetail> inventoryDetailMap = inventoryDetailList.stream().collect(Collectors.toMap(
                InventoryDetail::getId, d -> d
        ));
        int count = 0;
        for (InventoryDetailSubmitList inventoryDetailSubmitList : param.getDetailList()) {
          InventoryDetail detail =  inventoryDetailMap.get(inventoryDetailSubmitList.getDetailId());
          if (detail == null){
              throw new CommonException(404, "资产明细不存在");
          }
          detail.setResult(inventoryDetailSubmitList.getResult());
          detail.setActualUserId(inventoryDetailSubmitList.getActualUserId());
          detail.setActualLocation(inventoryDetailSubmitList.getActualLocation());
          detail.setRemark(inventoryDetailSubmitList.getRemark());
          detail.setCheckedAt(LocalDateTime.now());
          detail.setUpdatedAt(LocalDateTime.now());
          inventoryDetailMapper.updateById(detail);
          count ++;
        }
        if ("PENDING".equals(inventoryTask.getStatus())) {
            inventoryTask.setStatus("IN_PROGRESS");
            inventoryTask.setUpdatedAt(LocalDateTime.now());
            inventoryTaskMapper.updateById(inventoryTask);
        }
        InventoryDetailSubmitVO vo = new InventoryDetailSubmitVO();
        vo.setSuccess(true);
        vo.setSubmittedCount(count);
        return vo;

    }

}
