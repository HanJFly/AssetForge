package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.InventoryDetail;
import com.hjf.param.InventoryDetailPageParam;
import com.hjf.vo.InventoryDetailPageRecord;

import java.util.List;

/**
 * <p>
 * 盘点明细表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface InventoryDetailMapper extends BaseMapper<InventoryDetail> {

    List<InventoryDetailPageRecord> queryPage(InventoryDetailPageParam param);
}
