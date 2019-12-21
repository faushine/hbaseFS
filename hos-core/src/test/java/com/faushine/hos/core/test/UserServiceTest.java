package com.faushine.hos.core.test;

import com.faushine.hos.core.usermgr.model.SystemRole;
import com.faushine.hos.core.usermgr.model.UserInfo;
import com.faushine.hos.core.usermgr.IUserService;
import com.faushine.hos.mybatis.test.BaseTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public class UserServiceTest extends BaseTest {
  @Autowired
  @Qualifier("userServiceImpl")
  IUserService userService;

  @Test
  public void addUserTest(){
    UserInfo userInfo = new UserInfo("Tome","123","this is a test user", SystemRole.ADMIN);
    userService.addUser(userInfo);
  }

  @Test
  public void getUserTest(){
    UserInfo userInfo = userService.getUserInfoByName("Tome");
    System.out.println(userInfo.getUserName()+"|"+userInfo.getPassword());
  }

}
