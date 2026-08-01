package com.hjf.service;

import com.hjf.entity.InventoryTask;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.*;
import com.hjf.vo.*;

/**
 * <p>
 * 盘点任务表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IInventoryTaskService extends IService<InventoryTask> {

    InventoryTaskCreateVO create(InventoryTaskCreateParam param);

    InventoryTaskPageVO queryPage(InventoryTaskPageParam param);

    InventoryTaskDeatilVO detail(InventoryTaskDeatilParam param);

    InventoryTaskReportVO report(InventoryTaskReportParam param);

    InventoryTaskConclusionVO updateConclusion(InventoryTaskConclusionParam param);
}
