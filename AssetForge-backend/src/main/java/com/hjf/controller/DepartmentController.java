package com.hjf.controller;

import com.github.pagehelper.Page;
import com.hjf.common.result.Result;
import com.hjf.entity.Department;
import com.hjf.mapper.DepartmentMapper;
import com.hjf.param.DepartmentCreateParam;
import com.hjf.param.DepartmentDeleteParam;
import com.hjf.param.DepartmentUpdateParam;
import com.hjf.service.IDepartmentService;
import com.hjf.param.DepartmentPageParam;
import com.hjf.vo.DepartmentPageVO;
import com.hjf.vo.DepartmentVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 部门表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

    @PostMapping("/getAll")
    public Result<List<Department>> getDepartments() {
        return Result.ok(departmentService.getDepartments());
    }
    /*
    * 获取部门树
    * */
    @PostMapping("/tree")
    public Result<List<DepartmentVO>> getTree() {

        return Result.ok(departmentService.getTree());
    }

    /*
    * 部门分页查询
    * */
    @PostMapping("/page")
    public Result<DepartmentPageVO> page(@RequestBody DepartmentPageParam department ) {
        return Result.ok(departmentService.querypage(department));
    }


    /*
    * 创建部门
    * */
    @PostMapping("/create")
    public Result<Void> create(@RequestBody @Valid DepartmentCreateParam  departmentCreateParam){

        departmentService.createDepartment(departmentCreateParam);
        return Result.ok();
    }



    //部门详情
    @PostMapping("/detail")
    public Result<DepartmentVO> detail(@RequestBody DepartmentDeleteParam departmentDeleteParam){

        return Result.ok(departmentService.getDepartmentById(departmentDeleteParam.getId()));
    }
    /*
    * 修改部门
    * */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody DepartmentUpdateParam departmentUpdateParam){
        departmentService.updateDepartment(departmentUpdateParam);
        return Result.ok();
    }

    @PostMapping("/delete")
    public Result<Void> delate(@RequestBody DepartmentDeleteParam departmentDeleteParam){
        departmentService.delateDepartment(departmentDeleteParam);
        return Result.ok();
    }


}
