package com.hjf.param;

import com.hjf.entity.User;
import com.hjf.vo.Page;
import lombok.Data;

@Data
public class UserPageParam extends Page {
    private String username;
    private String realName;
    private Long departmentId;
    private String status;

}
