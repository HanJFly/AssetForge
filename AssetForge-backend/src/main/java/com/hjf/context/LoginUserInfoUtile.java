package com.hjf.context;


import com.hjf.param.LoginUserContext;

public class LoginUserInfoUtile {
    private static final ThreadLocal<LoginUserContext> HOLDER = new ThreadLocal<>();

    public static void set(LoginUserContext loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUserContext get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUserContext loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.getId();
    }

    public static String getUsername() {
        LoginUserContext loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.getUsername();
    }


    public static void clear() {
        HOLDER.remove();
    }

}
