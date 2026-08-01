package com.hjf.controller;

import com.github.pagehelper.Page;
import com.hjf.common.result.Result;
import com.hjf.param.AssetCategoryPageParam;
import com.hjf.param.AssetCategoryParam;
import com.hjf.service.IAssetCategoryService;
import com.hjf.vo.AssetCategoryPageVO;
import com.hjf.vo.AssetCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 资产分类表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/category")
public class AssetCategoryController {

    @Autowired
    private IAssetCategoryService assetCategoryService;

    /*
    * 获取资产分类树
    * */
    @PostMapping("/tree")
    public Result<List<AssetCategoryVO>> getTree(){

        return Result.ok(assetCategoryService.getTree());
    }

    /*
    * 分页查询资产分类
    * */
    @PostMapping("/page")
    public Result<AssetCategoryPageVO> queryPage(@RequestBody AssetCategoryPageParam param){
        return Result.ok(assetCategoryService.queryPage(param));
    }

    /*
    * 分类详情
    * */
    @PostMapping("/detail")
    public Result<AssetCategoryVO> detail(@RequestBody AssetCategoryParam  param){
        return Result.ok(assetCategoryService.detail(param));
    }

    /*
    * 新增资产分类
    * */
    @PostMapping("/create")
    public Result<Void> create(@RequestBody AssetCategoryParam  param){
        assetCategoryService.create(param);
        return Result.ok();
    }

    /*
    * 修改资产分类
    * */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody AssetCategoryParam  param){
        assetCategoryService.update(param);
        return Result.ok();
    }

    /*
    * 删除资产分类
    * */

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody AssetCategoryParam  param){
        assetCategoryService.delete(param);
        return Result.ok();
    }
}
