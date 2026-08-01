package com.hjf.service;

import com.hjf.entity.InventoryDetail;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.InventoryDetailPageParam;
import com.hjf.param.InventoryDetailSubmitParam;
import com.hjf.vo.InventoryDetailPageVO;
import com.hjf.vo.InventoryDetailSubmitVO;
import com.hjf.vo.InventoryTaskCreateVO;
import jakarta.validation.Valid;

/**
 * <p>
 * 盘点明细表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IInventoryDetailService extends IService<InventoryDetail> {


    InventoryDetailPageVO queryPage(InventoryDetailPageParam param);

    InventoryDetailSubmitVO submit(@Valid InventoryDetailSubmitParam param);
}
