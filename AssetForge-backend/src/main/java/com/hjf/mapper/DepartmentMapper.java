package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.Department;
import com.hjf.param.DepartmentPageParam;
import com.hjf.vo.DepartmentVO;

import java.util.List;

/**
 * <p>
 * 部门表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface DepartmentMapper extends BaseMapper<Department> {

    List<DepartmentVO> getTree();

    List<DepartmentVO> querypage(DepartmentPageParam departmentPageParam);


}
