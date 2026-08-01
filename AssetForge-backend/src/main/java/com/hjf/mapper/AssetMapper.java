package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.Asset;
import com.hjf.param.AssetPageParam;
import com.hjf.param.AssetParam;
import com.hjf.vo.AssetPageVO;
import com.hjf.vo.AssetVO;

import java.util.List;

/**
 * <p>
 * 资产表（管理信息） Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface AssetMapper extends BaseMapper<Asset> {

    List<AssetVO> queryPage(AssetPageParam param);
}
