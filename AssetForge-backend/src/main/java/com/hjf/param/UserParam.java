package com.hjf.param;

import com.hjf.entity.User;
import lombok.Data;

import java.lang.reflect.Array;
import java.util.List;

@Data
public class UserParam extends User {
    private String password;
    private List<Long> roleIds;
}
