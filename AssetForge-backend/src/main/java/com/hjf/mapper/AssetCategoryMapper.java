package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.AssetCategory;
import com.hjf.param.AssetCategoryPageParam;
import com.hjf.vo.AssetCategoryVO;

import java.util.List;

/**
 * <p>
 * 资产分类表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface AssetCategoryMapper extends BaseMapper<AssetCategory> {

    List<AssetCategoryVO> getTree();

    List<AssetCategoryVO> queryPage(AssetCategoryPageParam param);
}
