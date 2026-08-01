package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户角色关联表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    void insertUserRole(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    void updateById(@Param("userId") Long id, @Param("roleIds") List<Long> roleIds);
    void deleteByUserId(@Param("userId") Long userId);
}
