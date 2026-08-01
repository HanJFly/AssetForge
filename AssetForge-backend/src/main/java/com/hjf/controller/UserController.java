package com.hjf.controller;

import com.github.pagehelper.Page;
import com.hjf.common.result.Result;
import com.hjf.param.ResetPasswordParam;
import com.hjf.param.UserPageParam;
import com.hjf.param.UserParam;
import com.hjf.service.IUserService;
import com.hjf.service.impl.UserServiceImpl;

import com.hjf.vo.RoleListVO;
import com.hjf.vo.UserPageVO;
import com.hjf.vo.UserVO;
import jakarta.validation.spi.ValidationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /*
    * 分页查询用户列表
    * */
    @PostMapping("/page")
    public Result<UserPageVO> page(@RequestBody UserPageParam param){

        return Result.ok(userService.queryPage(param));
    }

    /*
    * 用户详情
    * */
    @PostMapping("/detail")
    public Result<UserVO> detail(@RequestBody UserParam  param){

       return Result.ok(userService.getUserById(param.getId()));
    }
    /*
    * 新增用户
    * */
    @PostMapping("/create")
    public Result<Void> create(@RequestBody UserParam  param){
        userService.createUser(param);
        return Result.ok();
    }

    /*
    * 修改用户
    * */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody UserParam  param){
        userService.updateUser(param);
        return Result.ok();
    }

    /*
    * 重置密码
    * */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordParam param){
        userService.resetPassword(param);
        return Result.ok();
    }

    //删除用户
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody UserParam  param){
        userService.delateUser(param);
        return Result.ok();
    }

    //角色列表
    @PostMapping("/role/list")
    public Result<List<RoleListVO>> roleList(@RequestBody UserParam  param){
        List<RoleListVO> roleListVO = userService.getRoleList(param);
        return Result.ok(roleListVO);
    }



}
