package com.faushine.hos.core.usermgr.dao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import com.faushine.hos.core.usermgr.model.UserInfo;
/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
@Mapper
public interface UserInfoMapper {

  void addUser(@Param("userInfo") UserInfo userInfo);

  int updateUserInfo(@Param("userId") String userId, @Param("password") String password,
      @Param("detail") String detail);

  int deleteUser(@Param("userId") String userId);

  @ResultMap("UserInfoResultMap")
  UserInfo getUserInfo(@Param("userId") String userId);

  @ResultMap("UserInfoResultMap")
  UserInfo getUserInfoByName(@Param("userName") String userName);

  UserInfo checkPassword(@Param("userName") String userName,
      @Param("password") String password);

}
