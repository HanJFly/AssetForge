package com.hjf.service.impl;

import com.hjf.entity.Role;
import com.hjf.mapper.RoleMapper;
import com.hjf.service.IRoleService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
