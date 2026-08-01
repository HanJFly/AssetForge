
package com.hjf.context;


import com.hjf.param.LoginUserContext;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Component
public class LoginUserIRoleUtile {
    @Resource
    private StringRedisTemplate stringRedisTemplate;




    public  void setRole (String userId, String roleId){

        stringRedisTemplate.opsForValue().set(userId, roleId);
    }

    public String getRole (String userId){
        return  stringRedisTemplate.opsForValue().get(userId);
    }
}
