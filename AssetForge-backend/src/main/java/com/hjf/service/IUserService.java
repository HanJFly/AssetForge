package com.hjf.service;

import com.github.pagehelper.Page;
import com.hjf.entity.User;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.ResetPasswordParam;
import com.hjf.param.UserPageParam;

import com.hjf.param.UserParam;
import com.hjf.vo.RoleListVO;
import com.hjf.vo.UserPageVO;
import com.hjf.vo.UserVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IUserService extends IService<User> {

    UserPageVO queryPage(UserPageParam param);

    UserVO getUserById(Long id);

    void createUser(UserParam param);

    void updateUser(UserParam param);

    void resetPassword(ResetPasswordParam param);

    void delateUser(UserParam param);

    List<RoleListVO> getRoleList(UserParam param);
}
