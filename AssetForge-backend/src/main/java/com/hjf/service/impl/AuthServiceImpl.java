package com.hjf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.common.result.CommonException;
import com.hjf.context.LoginUserIRoleUtile;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.ApprovalRecord;
import com.hjf.entity.Role;
import com.hjf.entity.User;
import com.hjf.entity.UserRole;
import com.hjf.mapper.ApprovalRecordMapper;
import com.hjf.mapper.RoleMapper;
import com.hjf.mapper.UserMapper;
import com.hjf.mapper.UserRoleMapper;
import com.hjf.param.LoginParam;
import com.hjf.param.LoginUserContext;
import com.hjf.param.SelectRoleParam;
import com.hjf.service.IApprovalRecordService;
import com.hjf.service.IAuthService;
import com.hjf.util.JwtUtils;
import com.hjf.util.PasswordUtils;
import com.hjf.vo.LoginUserListVO;
import com.hjf.vo.LoginVO;
import com.hjf.vo.RoleListVO;
import com.hjf.vo.SelectRoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LoginUserIRoleUtile loginUserIRoleUtile;



    /*
    * 登录方法
    * */

    @Override
    public LoginVO login(LoginParam param) {
        // 1. 先根据用户名查询用户
        QueryWrapper qw = new QueryWrapper();
        qw.eq("username", param.getUsername());
        User user = userMapper.selectOne(qw);

        // 2. 再用 BCrypt 验证密码（不能对密码重新 hash 后做等值比较，因为 BCrypt 每次 hash 结果不同）
        if (user == null || !PasswordUtils.matches(param.getPassword(), user.getPasswordHash())) {
            throw new CommonException(400, "用户名或密码错误");
        }
        // 生成JWT令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("real_name", user.getRealName());
        String token = jwtUtils.generateJwt(claims);

        log.info("生成的JWT令牌：{}", token);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer ");
        loginVO.setExpiresIn(jwtUtils.getExpire());
        //设置用户信息VO
        LoginUserListVO loginUserListVO = new LoginUserListVO();
        loginUserListVO.setId(user.getId());
        loginUserListVO.setUsername(user.getUsername());
        loginUserListVO.setRealName(user.getRealName());
        loginUserListVO.setEmployeeNo(user.getEmployeeNo());
        loginUserListVO.setDepartmentId(user.getDepartmentId());
        loginUserListVO.setDepartmentName(user.getDepartmentName());
        loginUserListVO.setStatus(user.getStatus());

        //设置角色信息VO  利用下面写的getRoleList方法获取用户角色列表
        List<RoleListVO> roleListVOList = getRoleList(user.getId());
        loginUserListVO.setRoles(roleListVOList);
        loginVO.setUser(List.of(loginUserListVO));

        return loginVO;


    }

    /*
    * 选择角色
    * */
    @Override
    public SelectRoleVO selectRole(SelectRoleParam param) {
        //判断角色是否存在
        LambdaQueryWrapper<Role> roleByRoleID = new LambdaQueryWrapper<>();
        roleByRoleID.eq(Role::getId,param.getRoleId());
        Role role = roleMapper.selectOne(roleByRoleID);
        if(role == null){
            throw new CommonException(404, "角色不存在");
        }
        //判断该用户是否已经拥有该角色
        //获取当前登录用户
        LoginUserContext context = LoginUserInfoUtile.get();;
        LambdaQueryWrapper<UserRole> queryByroleIDAndUserId = new LambdaQueryWrapper<>();
        queryByroleIDAndUserId.eq(UserRole::getUserId, context.getId());
        queryByroleIDAndUserId.eq(UserRole::getRoleId, param.getRoleId());
        UserRole userRole = userRoleMapper.selectOne(queryByroleIDAndUserId);
        if(userRole == null){
            throw new CommonException(400, "该用户未拥有该角色");
        }
        //设置用户角色到redis
        loginUserIRoleUtile.setRole(String.valueOf(context.getId()), String.valueOf(param.getRoleId()));
        SelectRoleVO selectRoleVO = new SelectRoleVO();
        selectRoleVO.setUserId(context.getId());
        selectRoleVO.setRoleId(param.getRoleId());
        selectRoleVO.setRoleCode(role.getCode());
        selectRoleVO.setRoleName(role.getName());
        return selectRoleVO;
    }

    /*
    * 获取当前登录用户信息
    * */
    @Override
    public LoginUserListVO getMe() {
        LoginUserContext context = LoginUserInfoUtile.get();
        LoginUserListVO loginUserListVO = new LoginUserListVO();
        loginUserListVO.setId(context.getId());
        loginUserListVO.setUsername(context.getUsername());
        loginUserListVO.setRealName(context.getRealName());
        User user = userMapper.selectById(context.getId());
        loginUserListVO.setEmployeeNo(user.getEmployeeNo());
        loginUserListVO.setPhone(user.getPhone());
        loginUserListVO.setDepartmentId(user.getDepartmentId());
        loginUserListVO.setDepartmentName(user.getDepartmentName());
        loginUserListVO.setStatus(user.getStatus());
        //设置角色信息VO  利用下面写的getRoleList方法获取用户角色列表
        List<RoleListVO> roleListVOList = getRoleList(context.getId());
        loginUserListVO.setRoles(roleListVOList);
        return loginUserListVO;
    }


    //获取用户角色列表的方法
    private List<RoleListVO> getRoleList(Long userId){
        List<RoleListVO> roleListVOList = new ArrayList<>();
        LambdaQueryWrapper<UserRole> getRoleIdByUserId = new LambdaQueryWrapper<>();
        getRoleIdByUserId.eq(UserRole::getUserId, userId);
        List<UserRole> userRoleList = userRoleMapper.selectList(getRoleIdByUserId);

        LambdaQueryWrapper<Role> getRoleById = new LambdaQueryWrapper<>();
        getRoleById.in(Role::getId,userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
        List<Role> roleList = roleMapper.selectList(getRoleById);
        Map<Long,String> roleMap = roleList.stream().collect(Collectors.toMap(Role::getId,Role::getName));
        Map<Long,String> roleCodeMap = roleList.stream().collect(Collectors.toMap(Role::getId,Role::getCode));
        for (UserRole userRole : userRoleList) {
            RoleListVO roleListVO = new RoleListVO();
            roleListVO.setId(userRole.getRoleId());
            roleListVO.setName(roleMap.get(userRole.getRoleId()));
            roleListVO.setCode(roleCodeMap.get(userRole.getRoleId()));
            roleListVOList.add(roleListVO);
        }
        return roleListVOList;
    }
}
