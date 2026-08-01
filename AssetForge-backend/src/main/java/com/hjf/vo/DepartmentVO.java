package com.hjf.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hjf.entity.Department;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DepartmentVO extends Department {


    /**
     * 子部门
     */
   private List<DepartmentVO> children = new ArrayList<>();


    private String managerUserName;

}
