package com.hjf.param;

import com.hjf.entity.User;
import com.hjf.vo.Page;
import lombok.Data;

@Data
public class DepartmentPageParam extends Page {
    private String name;

}
