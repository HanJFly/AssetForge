package com.hjf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.common.result.Result;
import com.hjf.entity.Asset;
import com.hjf.entity.Department;
import com.hjf.entity.User;
import com.hjf.entity.UserRole;
import com.hjf.mapper.AssetMapper;
import com.hjf.mapper.DepartmentMapper;
import com.hjf.mapper.UserMapper;
import com.hjf.mapper.UserRoleMapper;
import com.hjf.param.DepartmentCreateParam;
import com.hjf.param.DepartmentDeleteParam;
import com.hjf.param.DepartmentUpdateParam;
import com.hjf.service.IDepartmentService;

import com.hjf.param.DepartmentPageParam;
import com.hjf.vo.DepartmentPageVO;
import com.hjf.vo.DepartmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {


    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AssetMapper assetMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Override
    public List<Department> getDepartments() {
        List<Department> departments = departmentMapper.selectList(new QueryWrapper<>());
        return departments;
    }

    @Override
    public List<DepartmentVO> getTree() {
        //获取所有部门
        List<DepartmentVO> departments = departmentMapper.getTree();
        //构建树形结构
        Map<Long, List<DepartmentVO>> departmentMap = departments.stream().collect(Collectors.groupingBy(
                d -> d.getParentId() == null ? 0L : d.getParentId()
        ));
        //构建子类
        for (DepartmentVO department : departments) {
            List<DepartmentVO> children = departmentMap.get(department.getId());
            department.setChildren(children == null ? new ArrayList<>() : children);
        }

        //获取顶级部门
        List<DepartmentVO> topDepartments = departments.stream()
                .filter(d ->d.getParentId() == null || d.getParentId() == 0)
                .collect(Collectors.toList());
        return topDepartments;
    }



    /*
    * 分页查询部门
    * */
    @Override
    public DepartmentPageVO querypage(DepartmentPageParam department) {
        //分页
        PageHelper.startPage(department.getPage(), department.getSize());
        //查询
        List<DepartmentVO> departments = departmentMapper.querypage(department);

        Page<DepartmentVO> pageInfo = (Page<DepartmentVO>) departments;

        DepartmentPageVO departmentPageVO = new DepartmentPageVO();
        departmentPageVO.setTotal(pageInfo.getTotal());
        departmentPageVO.setRecords(pageInfo);
        departmentPageVO.setPage(pageInfo.getPageNum());
        departmentPageVO.setSize(pageInfo.getPageSize());
        return departmentPageVO;
    }

    /*
    * 创建部门
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDepartment(DepartmentCreateParam departmentCreateParam) {
        //判断部门名称是否存在
        if(departmentCreateParam.getName() == null){
            throw new CommonException(400, "部门名称不能为空");
        }
        //判断部门名称是否已经存在
        if(departmentMapper.selectOne(new QueryWrapper<Department>().eq("name" , departmentCreateParam.getName())) != null){
            throw new CommonException(400, "部门名称已经存在");
        }
        //如果指定了父级部门，判断父级部门是否存在
        if(departmentCreateParam.getParentId() != null && departmentMapper.selectOne(new QueryWrapper<Department>().eq("id", departmentCreateParam.getParentId())) == null){
            throw new CommonException(404, "父级部门不存在");
        }
        //检查同级下部门名称是否已经存在
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("name" , departmentCreateParam.getName());
        qw.eq("parent_id" , departmentCreateParam.getParentId() != null ? departmentCreateParam.getParentId(): 0);
        qw.eq("is_deleted" , 0);
        if(departmentMapper.selectCount(qw) > 0){
            throw new CommonException(400, "同级下部门名称已经存在");
        }
        //给指定的部门管理员添加角色
        LambdaQueryWrapper<UserRole> queryWrapperRoleId = new LambdaQueryWrapper<>();
        queryWrapperRoleId.eq(UserRole::getUserId, departmentCreateParam.getManagerUserId());
        queryWrapperRoleId.eq(UserRole::getRoleId, 4);
        if(userRoleMapper.selectOne(queryWrapperRoleId) == null){
            UserRole userRole = new UserRole();
            userRole.setUserId(departmentCreateParam.getManagerUserId());
            userRole.setRoleId((long) 4);
            userRoleMapper.insert(userRole);
        }



        //创建部门
        Department department = new Department();
        department.setName(departmentCreateParam.getName());
        department.setParentId(departmentCreateParam.getParentId());
        department.setManagerUserId(departmentCreateParam.getManagerUserId());
        department.setSortOrder(departmentCreateParam.getSortOrder()!= null ? departmentCreateParam.getSortOrder() : 0);
        departmentMapper.insert(department);
    }


    /*
    * 根据id查询部门
    * */
    @Override
    public DepartmentVO getDepartmentById(Long id) {
        Department department = departmentMapper.selectById(id);
        DepartmentVO departmentVO = BeanUtil.copyProperties(department, DepartmentVO.class);
        return departmentVO;
    }

    /*
    * 修改部门
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(DepartmentUpdateParam departmentUpdateParam) {
        Department department = departmentMapper.selectById(departmentUpdateParam.getId());
        if(department == null){
            throw new CommonException(404, "部门不存在");
        }
        Long oldManagerUserId = department.getManagerUserId();

        department.setName(departmentUpdateParam.getName());
        department.setParentId(departmentUpdateParam.getParentId()!=null ? departmentUpdateParam.getParentId() : 0);
        department.setManagerUserId(departmentUpdateParam.getManagerUserId());
        department.setSortOrder(departmentUpdateParam.getSortOrder()!=null ? departmentUpdateParam.getSortOrder() : 0);
        departmentMapper.updateById(department);

        //给指定的部门管理员添加角色
        LambdaQueryWrapper<UserRole> queryWrapperRoleId = new LambdaQueryWrapper<>();
        queryWrapperRoleId.eq(UserRole::getUserId, departmentUpdateParam.getManagerUserId());
        queryWrapperRoleId.eq(UserRole::getRoleId, 4);
        if(userRoleMapper.selectOne(queryWrapperRoleId) == null){
            UserRole userRole = new UserRole();
            userRole.setUserId(departmentUpdateParam.getManagerUserId());
            userRole.setRoleId((long) 4);
            userRoleMapper.insert(userRole);
        }

        //删除旧的管理员角色
        LambdaQueryWrapper<Department> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Department::getManagerUserId, oldManagerUserId);
        queryWrapper.eq(Department::getIsDeleted, 0);
        if(departmentMapper.selectCount(queryWrapper) == 0){
            LambdaQueryWrapper<UserRole> queryWrapperByRoleId = new LambdaQueryWrapper<>();
            queryWrapperByRoleId.eq(UserRole::getUserId, oldManagerUserId);
            queryWrapperByRoleId.eq(UserRole::getRoleId, 4);
            userRoleMapper.delete(queryWrapperByRoleId);
        }

    }



    /*
    * 删除部门
    * */
    @Override
    public void delateDepartment(DepartmentDeleteParam departmentDeleteParam) {
        Department department = departmentMapper.selectById(departmentDeleteParam.getId());
        //校验部门是否存在
        if(department == null){
            throw new CommonException( 500,"部门不存在");
        }

        //校验是否存在子部门
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("parent_id" , department.getId());
        Long l = departmentMapper.selectCount(qw);
        if(l>0){
            throw new CommonException( 500,"存在子部门，请先删除子部门");
        }

        //校验部门下是否存在员工
        QueryWrapper<User> qwu = new QueryWrapper<>();
        qwu.eq("department_id", department.getId());
        Long l1 = userMapper.selectCount(qwu);
        if(l1>0){
            throw new CommonException( 500,"部门下存在员工，请先将员工移出部门");
        }

        //校验部门下是否有资产关联
        QueryWrapper<Asset> qwa = new QueryWrapper<>();
        qwa.eq("department_id", department.getId());
        Long l2 = assetMapper.selectCount(qwa);
        if (l2>0){
            throw new CommonException( 500,"部门下存在资产，请先将资产移出部门");
        }



        //删除部门,软删除
        department.setIsDeleted((byte) 1);
        departmentMapper.updateById(department);
        //检验部门管理员是否是其他部门的部门管理员，如果不是，删除user_role的信息
        LambdaQueryWrapper<Department> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Department::getManagerUserId, department.getManagerUserId());
        queryWrapper.eq(Department::getIsDeleted, 0);
        if(departmentMapper.selectCount(queryWrapper) == 0){
            LambdaQueryWrapper<UserRole> queryWrapperRoleId = new LambdaQueryWrapper<>();
            queryWrapperRoleId.eq(UserRole::getUserId, department.getManagerUserId());
            queryWrapperRoleId.eq(UserRole::getRoleId, 4);
            userRoleMapper.delete(queryWrapperRoleId);
        }
    }


}
