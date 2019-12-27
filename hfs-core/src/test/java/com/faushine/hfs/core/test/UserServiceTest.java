package com.faushine.hfs.core.test;

import com.faushine.hfs.core.usermgr.IUserService;
import com.faushine.hfs.core.usermgr.model.SystemRole;
import com.faushine.hfs.core.usermgr.model.UserInfo;
import com.faushine.hfs.mybatis.test.BaseTest;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
@Ignore
public class UserServiceTest extends BaseTest {
  @Autowired
  @Qualifier("userServiceImpl")
  IUserService userService;

  @Test
  public void addUserTest(){
    UserInfo userInfo = new UserInfo("Tom","123","this is a test user", SystemRole.ADMIN);
    userService.addUser(userInfo);
  }

  @Test
  public void getUserTest(){
    UserInfo userInfo = userService.getUserInfoByName("Tom");
    Assert.assertNotNull(userInfo.getUserName());
    Assert.assertNotNull(userInfo.getPassword());
  }

  @Test
  public void updateUserInfo(){
    UserInfo userInfo = userService.getUserInfoByName("Tom");
    userService.updateUserInfo(userInfo.getUserId(),userInfo.getPassword(),"this is a test updated user");
    userInfo = userService.getUserInfoByName("Tom");
    Assert.assertEquals(userInfo.getDetail(),"this is a test updated user");
  }

  @Test
  public void checkPassword(){
    UserInfo userInfo = userService.getUserInfoByName("Tom");
    userInfo = userService.checkPassword(userInfo.getUserName(),"123");
    Assert.assertNotNull(userInfo);
  }

  @Test
  public void deleteUser(){
    UserInfo userInfo = userService.getUserInfoByName("Tom");
    Assert.assertTrue(userService.deleteUser(userInfo.getUserId()));
  }

}
