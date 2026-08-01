package com.hjf.service;

import com.github.pagehelper.Page;
import com.hjf.common.result.Result;
import com.hjf.entity.AssetCategory;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.AssetCategoryPageParam;
import com.hjf.param.AssetCategoryParam;
import com.hjf.vo.AssetCategoryPageVO;
import com.hjf.vo.AssetCategoryVO;

import java.util.List;

/**
 * <p>
 * 资产分类表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IAssetCategoryService extends IService<AssetCategory> {


    List<AssetCategoryVO> getTree();

    AssetCategoryPageVO queryPage(AssetCategoryPageParam param);

    AssetCategoryVO detail(AssetCategoryParam param);


    void create(AssetCategoryParam param);

    void update(AssetCategoryParam param);

    void delete(AssetCategoryParam param);
}
