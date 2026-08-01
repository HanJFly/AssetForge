package com.hjf.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjf.common.result.Result;
import com.hjf.entity.User;
import com.hjf.mapper.UserMapper;
import com.hjf.param.LoginParam;
import com.hjf.param.SelectRoleParam;
import com.hjf.service.IAuthService;
import com.hjf.service.IUserService;
import com.hjf.vo.LoginUserListVO;
import com.hjf.vo.LoginVO;
import com.hjf.vo.SelectRoleVO;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;


    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginParam param){
       LoginVO loginVO = authService.login(param);
       return Result.ok(loginVO);
    }

    @PostMapping("/select-role")
    public Result<SelectRoleVO> selectRole(@RequestBody SelectRoleParam param){
        SelectRoleVO selectRoleVO = authService.selectRole(param);
        return Result.ok(selectRoleVO);
    }

    @PostMapping("/me")
    public Result<LoginUserListVO> getMe(){
        LoginUserListVO loginUserListVO = authService.getMe();
        return Result.ok(loginUserListVO);
    }
}
