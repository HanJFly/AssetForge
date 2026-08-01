package com.hjf.service;

import com.hjf.entity.DepreciationRunLog;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.DepreciationRunLogExecuteParam;
import com.hjf.param.DepreciationRunLogPageParam;
import com.hjf.vo.DepreciationRunLogExecuteVO;
import com.hjf.vo.DepreciationRunLogPageVO;

/**
 * <p>
 * 月度折旧执行记录表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IDepreciationRunLogService extends IService<DepreciationRunLog> {

    DepreciationRunLogExecuteVO execute(DepreciationRunLogExecuteParam param);

    DepreciationRunLogPageVO qurypage(DepreciationRunLogPageParam param);
}
