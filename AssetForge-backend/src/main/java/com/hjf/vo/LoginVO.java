package com.hjf.vo;


import com.hjf.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoginVO {
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private List<LoginUserListVO> user = new ArrayList<>();
}
