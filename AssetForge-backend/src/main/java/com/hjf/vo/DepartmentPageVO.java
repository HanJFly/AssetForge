package com.hjf.vo;

import com.github.pagehelper.Page;
import lombok.Data;

@Data
public class DepartmentPageVO {
    private Page<DepartmentVO> records;
    private Long total;
    private Integer page;
    private Integer size;
}
