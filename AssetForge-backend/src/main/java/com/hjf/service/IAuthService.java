package com.hjf.service;

import com.hjf.param.LoginParam;
import com.hjf.param.SelectRoleParam;
import com.hjf.vo.LoginUserListVO;
import com.hjf.vo.LoginVO;
import com.hjf.vo.SelectRoleVO;

public interface IAuthService {
    LoginVO login(LoginParam param);

    SelectRoleVO selectRole(SelectRoleParam param);

    LoginUserListVO getMe();
}
