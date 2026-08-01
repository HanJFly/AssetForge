package com.hjf.vo;

import com.hjf.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserVO extends User {
    private List<UserVO> children = new ArrayList<>();
    private List<String> roleNames = new ArrayList<>();
}
