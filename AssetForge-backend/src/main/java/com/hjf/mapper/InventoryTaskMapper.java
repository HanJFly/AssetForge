package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.InventoryTask;
import com.hjf.param.InventoryTaskPageParam;
import com.hjf.param.InventoryTaskReportParam;
import com.hjf.vo.InventoryTaskPageRecord;
import com.hjf.vo.InventoryTaskReportVO;

import java.util.List;

/**
 * <p>
 * 盘点任务表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface InventoryTaskMapper extends BaseMapper<InventoryTask> {

    List<InventoryTaskPageRecord> queryPage(InventoryTaskPageParam param);

    InventoryTaskReportVO queryCount(InventoryTaskReportParam param);
}
