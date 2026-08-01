package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.User;
import com.hjf.param.UserPageParam;
import com.hjf.vo.UserVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface UserMapper extends BaseMapper<User> {


    List<UserVO> queryPage(UserPageParam param);


}
