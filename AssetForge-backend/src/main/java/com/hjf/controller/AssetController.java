package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.AssetCreateWithFilesParam;
import com.hjf.param.AssetPageParam;
import com.hjf.param.AssetParam;
import com.hjf.service.IAssetService;
import com.hjf.vo.AssetBarcodeDetailVO;
import com.hjf.vo.AssetCreateVO;
import com.hjf.vo.AssetPageVO;
import com.hjf.vo.AssetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 资产表（管理信息）前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/asset")
public class AssetController {

    @Autowired
    private IAssetService assetService;

    /*
    * 分页查询
    * */
    @RequestMapping("/page")
    public Result<AssetPageVO> page(@RequestBody AssetPageParam param) {
        return Result.ok(assetService.queryPage(param));
    }

    /*
    * 详情
    * */
    @RequestMapping("/detail")
    public Result<AssetVO> detail(@RequestBody AssetParam param) {
        return Result.ok(assetService.detail(param));
    }

    /*
    * 新增资产
    * */
    @PostMapping("/create")
    public Result<AssetCreateVO> create(@RequestBody AssetParam param) {
        return Result.ok(assetService.create(param));
    }

    /*
    * 新增资产并上传附件
    * */
    @PostMapping("/create-with-files")
    public Result<AssetCreateVO> createWithFiles(@ModelAttribute AssetCreateWithFilesParam param,
                                                 @RequestParam("files") MultipartFile[] files) {
        return Result.ok(assetService.createWithFiles(param, files));
    }

    /*
    * 修改资产
    * */
    @RequestMapping("/update")
    public Result<Void> update(@RequestBody AssetParam param) {
        assetService.updateAsset(param);
        return Result.ok();
    }

    /*
    * 删除资产：只允许删除未被业务单据引用的资产
    * */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody AssetParam param) {
        assetService.deleteAsset(param);
        return Result.ok();
    }

    @PostMapping("/barcode/detail")
    public Result<AssetBarcodeDetailVO> barcodeDetail(@RequestBody AssetParam param) {
        return Result.ok(assetService.barcodeDetail(param));
    }
}
