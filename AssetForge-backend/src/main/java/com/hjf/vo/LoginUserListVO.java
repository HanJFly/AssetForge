package com.hjf.vo;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoginUserListVO {
    private Long id;
    private String username;
    private String realName;
    private String employeeNo;
    private Long departmentId;
    private String departmentName;
    private String status;
    private List<RoleListVO> roles = new ArrayList<>();

    private String phone;

}
