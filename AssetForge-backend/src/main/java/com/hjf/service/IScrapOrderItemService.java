package com.hjf.service;

import com.hjf.entity.ScrapOrderItem;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.ScrapOrderCreateParam;
import com.hjf.param.ScrapOrderDetailParam;
import com.hjf.param.ScrapOrderPageParam;
import com.hjf.vo.ScrapOrderCreateVO;
import com.hjf.vo.ScrapOrderDetailVO;
import com.hjf.vo.ScrapOrderPageVO;
import jakarta.validation.Valid;

/**
 * <p>
 * 报废单-资产明细表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IScrapOrderItemService extends IService<ScrapOrderItem> {

    ScrapOrderCreateVO create(@Valid ScrapOrderCreateParam param);

    ScrapOrderPageVO queryPage(ScrapOrderPageParam param);

    ScrapOrderDetailVO detail(ScrapOrderDetailParam param);
}
