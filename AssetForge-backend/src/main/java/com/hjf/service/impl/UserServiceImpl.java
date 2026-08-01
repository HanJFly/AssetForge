package com.hjf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.entity.ApprovalRecord;
import com.hjf.entity.Asset;
import com.hjf.entity.Department;
import com.hjf.entity.Role;
import com.hjf.entity.User;
import com.hjf.entity.UserRole;
import com.hjf.mapper.ApprovalRecordMapper;
import com.hjf.mapper.AssetMapper;
import com.hjf.mapper.DepartmentMapper;
import com.hjf.mapper.RoleMapper;
import com.hjf.mapper.UserMapper;
import com.hjf.mapper.UserRoleMapper;
import com.hjf.param.ResetPasswordParam;
import com.hjf.param.UserPageParam;
import com.hjf.param.UserParam;
import com.hjf.service.IUserService;
import com.hjf.util.PasswordUtils;
import com.hjf.vo.RoleListVO;
import com.hjf.vo.UserPageVO;
import com.hjf.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public UserPageVO queryPage(UserPageParam param) {

        PageHelper.startPage(param.getPage(), param.getSize());

        List<UserVO> users = userMapper.queryPage(param);

        LambdaQueryWrapper<UserRole> getRoleIdByUserIDWrapper = new LambdaQueryWrapper<>();
        getRoleIdByUserIDWrapper.in(UserRole::getUserId, users.stream().map(UserVO::getId).toList());
        List<UserRole> userRoles = userRoleMapper.selectList(getRoleIdByUserIDWrapper);
        LambdaQueryWrapper<Role> getRoleNameByRoleIdWrapper = new LambdaQueryWrapper<>();
        getRoleNameByRoleIdWrapper.in(Role::getId, userRoles.stream().map(UserRole::getRoleId).toList());
        List<Role> roles = roleMapper.selectList(getRoleNameByRoleIdWrapper);


        Map<Long, String> roleIdToRoleNameMap = roles.stream().collect(Collectors.toMap(Role::getId, Role::getName));


        // 封装角色名到每个用户
        Map<Long, List<String>> userIdToRoleNamesMap = userRoles.stream()
                .collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(
                                ur -> roleIdToRoleNameMap.get(ur.getRoleId()),
                                Collectors.toList()
                        )
                ));
        users.forEach(user -> {
            List<String> roleNames = userIdToRoleNamesMap.get(user.getId());
            if (roleNames != null) {
                user.setRoleNames(roleNames);
            }
        });


        Page<UserVO> pageInfo = (Page<UserVO>) users;

        UserPageVO userPageVO = new UserPageVO();
        userPageVO.setTotal(pageInfo.getTotal());
        userPageVO.setRecords(users);
        userPageVO.setSize(param.getSize());
        userPageVO.setPage(param.getPage());



        return userPageVO;
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        return BeanUtil.copyProperties(user, UserVO.class);
    }


    /*
    * 创建用户
    * */
    @Override
    public void createUser(UserParam param) {
        if (param.getUsername() == null) {
            throw new CommonException(500, "用户名不能为空");
        }

        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", param.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new CommonException(500, "用户名已存在");
        }

        if (param.getPassword() == null) {
            throw new CommonException(500, "密码不能为空");
        }

        if (param.getRealName() == null) {
            throw new CommonException(500, "真实姓名不能为空");
        }

        QueryWrapper<User> employeeNoWrapper = new QueryWrapper<>();
        employeeNoWrapper.eq("employee_no", param.getEmployeeNo());
        if (userMapper.selectCount(employeeNoWrapper) > 0) {
            throw new CommonException(500, "工号已存在");
        }
        if (!param.getEmail().isEmpty()){
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", param.getEmail());
            if (userMapper.selectCount(emailWrapper) > 0) {
                throw new CommonException(500, "邮箱已存在");
            }
        }

        if (param.getDepartmentId() == null) {
            param.setDepartmentId(0L);
        }

        Department department = departmentMapper.selectById(param.getDepartmentId());
        if (department != null) {
            param.setDepartmentName(department.getName());
        }

        if (param.getStatus() == null) {
            param.setStatus("正常");
        }


        param.setPasswordHash(PasswordUtils.hash(param.getPassword()));
        param.setIsDeleted((byte) 0);

        User user = BeanUtil.copyProperties(param, User.class);
        userMapper.insert(user);

        if (param.getRoleIds() != null && !param.getRoleIds().isEmpty()) {
            userRoleMapper.insertUserRole(user.getId(), param.getRoleIds());
        }
    }

    @Override
    public void updateUser(UserParam param) {
        User user = userMapper.selectById(param.getId());
        if (user == null) {
            throw new CommonException(500, "用户不存在");
        }

        if (param.getRealName() == null) {
            throw new CommonException(500, "真实姓名不能为空");
        }

        QueryWrapper<User> employeeNoWrapper = new QueryWrapper<>();
        employeeNoWrapper.eq("employee_no", param.getEmployeeNo());
        employeeNoWrapper.ne("id", param.getId());
        if (userMapper.selectCount(employeeNoWrapper) > 0) {
            throw new CommonException(500, "工号已存在");
        }

        if (param.getStatus() == null) {
            param.setStatus("正常");
        }

        user.setId(param.getId());
        user.setRealName(param.getRealName());
        user.setEmployeeNo(param.getEmployeeNo());
        user.setPhone(param.getPhone());
        user.setEmail(param.getEmail());
        user.setDepartmentId(param.getDepartmentId());

        QueryWrapper<Department> departmentWrapper = new QueryWrapper<>();
        departmentWrapper.eq("id", param.getDepartmentId());
        Department department = departmentMapper.selectOne(departmentWrapper);
        user.setDepartmentName(department != null ? department.getName() : null);
        user.setStatus(param.getStatus());
        userMapper.updateById(user);

        userRoleMapper.deleteByUserId(user.getId());
        if (param.getRoleIds() != null && !param.getRoleIds().isEmpty()) {
            userRoleMapper.insertUserRole(user.getId(), param.getRoleIds());
        }
    }

    @Override
    public void resetPassword(ResetPasswordParam param) {
        if (param.getNewPassword() == null || param.getNewPassword().isEmpty()) {
            throw new CommonException(500, "密码不能为空");
        }

        User user = userMapper.selectById(param.getId());
        if (user == null || user.getIsDeleted() == 1) {
            throw new CommonException(500, "用户不存在");
        }

        user.setPasswordHash(PasswordUtils.hash(param.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void delateUser(UserParam param) {
        User user = userMapper.selectById(param.getId());
        if (user == null || user.getIsDeleted() == 1) {
            throw new CommonException(500, "用户不存在");
        }

        QueryWrapper<Department> departmentWrapper = new QueryWrapper<>();
        departmentWrapper.eq("manager_user_id", param.getId());
        if (departmentMapper.selectCount(departmentWrapper) > 0) {
            throw new CommonException(500, "该用户为部门管理员，请先做好交接");
        }

        QueryWrapper<Asset> assetWrapper = new QueryWrapper<>();
        assetWrapper.eq("current_user_id", param.getId());
        if (assetMapper.selectCount(assetWrapper) > 0) {
            throw new CommonException(500, "该用户持有资产，请先处理资产");
        }

        QueryWrapper<ApprovalRecord> approvalWrapper = new QueryWrapper<>();
        approvalWrapper.eq("applicant_id", param.getId());
        approvalWrapper.eq("approval_status", "PENDING");
        if (approvalRecordMapper.selectCount(approvalWrapper) > 0) {
            throw new CommonException(500, "该用户有未完成的审批，请先处理审批");
        }

        user.setIsDeleted((byte) 1);
        userMapper.updateById(user);
        userRoleMapper.deleteByUserId(param.getId());
    }

    @Override
    public RoleListVO getRoleList(UserParam param) {
        User user = userMapper.selectById(param.getId());
        if (user == null || user.getIsDeleted() == 1) {
            throw new CommonException(500, "用户不存在");
        }

        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", param.getId());
        UserRole userRole = userRoleMapper.selectOne(wrapper);
        if (userRole == null) {
            throw new CommonException(404, "用户角色不存在");
        }

        QueryWrapper<Role> roleWrapper = new QueryWrapper<>();
        roleWrapper.eq("id", userRole.getRoleId());
        Role role = roleMapper.selectOne(roleWrapper);

        RoleListVO roleListVO = new RoleListVO();
        roleListVO.setId(role.getId());
        roleListVO.setCode(role.getCode());
        roleListVO.setName(role.getName());
        return roleListVO;
    }
}
