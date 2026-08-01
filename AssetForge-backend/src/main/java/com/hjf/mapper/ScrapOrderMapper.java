package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.ScrapOrder;
import com.hjf.param.ScrapOrderPageParam;
import com.hjf.vo.ScrapOrderPageRecord;

import java.util.List;

/**
 * <p>
 * 报废单主表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface ScrapOrderMapper extends BaseMapper<ScrapOrder> {

    List<ScrapOrderPageRecord> queryPage(ScrapOrderPageParam param);
}
