package com.hjf.service;

import com.github.pagehelper.Page;
import com.hjf.entity.Asset;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.AssetCreateWithFilesParam;
import com.hjf.param.AssetPageParam;
import com.hjf.param.AssetParam;
import com.hjf.vo.AssetBarcodeDetailVO;
import com.hjf.vo.AssetCreateVO;
import com.hjf.vo.AssetPageVO;
import com.hjf.vo.AssetVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 资产表（管理信息） 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IAssetService extends IService<Asset> {

    AssetPageVO queryPage(AssetPageParam param);

    AssetVO detail(AssetParam param);

    AssetCreateVO create(AssetParam param);

    AssetCreateVO createWithFiles(AssetCreateWithFilesParam param, MultipartFile[] files);

    void updateAsset(AssetParam param);

    void deleteAsset(AssetParam param);

    AssetBarcodeDetailVO barcodeDetail(AssetParam param);


}
