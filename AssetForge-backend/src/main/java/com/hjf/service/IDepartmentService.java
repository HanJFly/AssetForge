package com.hjf.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.github.pagehelper.Page;
import com.hjf.common.result.Result;
import com.hjf.entity.Department;

import com.hjf.param.DepartmentCreateParam;
import com.hjf.param.DepartmentDeleteParam;
import com.hjf.param.DepartmentPageParam;
import com.hjf.param.DepartmentUpdateParam;
import com.hjf.vo.DepartmentPageVO;
import com.hjf.vo.DepartmentVO;

import java.util.List;

/**
 * <p>
 * 部门表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IDepartmentService extends IService<Department> {

    List<Department> getDepartments();

    List<DepartmentVO> getTree();

    DepartmentPageVO querypage(DepartmentPageParam department);


    void createDepartment(DepartmentCreateParam departmentCreateParam);


    DepartmentVO getDepartmentById(Long id);

    void updateDepartment(DepartmentUpdateParam departmentUpdateParam);

    void delateDepartment(DepartmentDeleteParam departmentDeleteParam);
}
