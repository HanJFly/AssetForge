package com.hjf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjf.common.result.CommonException;
import com.hjf.entity.SystemConfig;
import com.hjf.mapper.SystemConfigMapper;
import com.hjf.param.SystemConfigParam;
import com.hjf.service.ISystemConfigService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.SystemConfigVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 系统配置表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements ISystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;
    /*
    * 详情
    * */
    @Override
    public List<SystemConfigVo> detail() {
        List<SystemConfig> systemConfigs = systemConfigMapper.selectList(new QueryWrapper<SystemConfig>());
        if(systemConfigs == null){
            return new ArrayList<>();
        }
        List<SystemConfigVo> systemConfigsVo = BeanUtil.copyToList(systemConfigs, SystemConfigVo.class);

        return systemConfigsVo;
    }

    /*
    * 修改
    * */
    @Override
    public void update(List<SystemConfigParam> param) {
        List<SystemConfig> systemConfigs = BeanUtil.copyToList(param, SystemConfig.class);
        for (SystemConfig systemConfig : systemConfigs) {
            Long id = systemConfig.getId();

            if (systemConfigMapper.selectById(id) == null){
                throw new CommonException(404, "修改失败，系统配置不存在");
            }
            systemConfigMapper.updateById(systemConfig);
        }
    }
}
