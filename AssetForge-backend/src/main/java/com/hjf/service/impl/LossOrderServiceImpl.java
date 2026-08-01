package com.hjf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.AssetLedger;
import com.hjf.entity.LossOrder;
import com.hjf.mapper.AssetLedgerMapper;
import com.hjf.mapper.LossOrderMapper;
import com.hjf.param.LoginUserContext;
import com.hjf.param.LossOrderDetailParam;
import com.hjf.param.LossOrderHandleParam;
import com.hjf.param.LossOrderPageParam;
import com.hjf.service.ILossOrderService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.LossOrderDetailVO;
import com.hjf.vo.LossOrderHandleVO;
import com.hjf.vo.LossOrderPageRecord;
import com.hjf.vo.LossOrderPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 盘亏单表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class LossOrderServiceImpl extends ServiceImpl<LossOrderMapper, LossOrder> implements ILossOrderService {

    @Autowired
    private LossOrderMapper lossOrderMapper;
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;
    @Override
    public LossOrderPageVO queryPage(LossOrderPageParam param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        //查询
        List<LossOrderPageRecord> list = lossOrderMapper.queryPage(param);
        Page<LossOrderPageRecord> pageInfo = (Page<LossOrderPageRecord>) list;
        LossOrderPageVO lossOrderPageVO = new LossOrderPageVO();
        lossOrderPageVO.setTotal(pageInfo.getTotal());
        lossOrderPageVO.setRecords(pageInfo);
        lossOrderPageVO.setPage(pageInfo.getPageNum());
        lossOrderPageVO.setPageSize(pageInfo.getPageSize());

        return lossOrderPageVO;

    }

    @Override
    public LossOrderDetailVO detail(LossOrderDetailParam param) {
        LossOrder lossOrder = lossOrderMapper.selectById(param.getId());
        if (lossOrder == null) {
            throw new CommonException(404, "盘亏单不存在");
        }
        LossOrderDetailVO vo = new LossOrderDetailVO();
        BeanUtil.copyProperties(lossOrder,vo);
        LambdaQueryWrapper<AssetLedger> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetLedger::getAssetId, lossOrder.getAssetId());
        AssetLedger assetLedger = assetLedgerMapper.selectOne(queryWrapper);
        if (assetLedger == null) {
            throw new CommonException(404, "资产台账不存在");
        }
        vo.setLossAmount(assetLedger.getNetValue());
        return vo;

    }

    @Override
    public LossOrderHandleVO handle(LossOrderHandleParam param) {
        LossOrder lossOrder = lossOrderMapper.selectById(param.getId());
        if (lossOrder == null){
            throw new CommonException(404, "盘亏单不存在");
        }
        if("COMPENSATED".equals(lossOrder.getCompensationStatus())){
            throw new CommonException(400, "盘亏单已处理");
        }
        if("EXEMPTED".equals(lossOrder.getCompensationStatus())){
            throw new CommonException(400, "盘亏单已豁免");
        }
        LoginUserContext context = LoginUserInfoUtile.get();
        switch (param.getHandleType()){
            case "PENDING_COMPENSATION":
                lossOrder.setCompensationStatus("COMPENSATED");
                lossOrder.setActualCompensation(param.getHandleAmount());
                lossOrder.setHandlingRemark(param.getHandlingRemark());
                lossOrder.setHandlerId(context.getId());
                lossOrderMapper.updateById(lossOrder);
                LossOrderHandleVO vo = new LossOrderHandleVO();
                vo.setSuccess(true);
                vo.setHandleStatus(lossOrder.getCompensationStatus());
                return vo;
            case "EXEMPTING":
                lossOrder.setCompensationStatus("EXEMPTED");
                lossOrder.setExemptionReason(param.getHandlingRemark());
                lossOrder.setHandlerId(context.getId());
                lossOrderMapper.updateById(lossOrder);
                LossOrderHandleVO voE = new LossOrderHandleVO();
                voE.setSuccess(true);
                voE.setHandleStatus(lossOrder.getCompensationStatus());
                return voE;

        }
        throw new CommonException(400, "无效的盘亏处理类型");

    }
}
