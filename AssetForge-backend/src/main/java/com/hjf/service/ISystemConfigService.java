package com.hjf.service;

import com.hjf.entity.SystemConfig;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.SystemConfigParam;
import com.hjf.vo.SystemConfigVo;

import java.util.List;

/**
 * <p>
 * 系统配置表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface ISystemConfigService extends IService<SystemConfig> {

    List<SystemConfigVo> detail();

    void update(List<SystemConfigParam> param);
}
