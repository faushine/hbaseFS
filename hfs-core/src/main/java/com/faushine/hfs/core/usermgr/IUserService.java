package com.faushine.hfs.core.usermgr;

import com.faushine.hfs.core.usermgr.model.UserInfo;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public interface IUserService {

  public boolean addUser(UserInfo userInfo);

  public boolean updateUserInfo(String userId, String password, String detail);

  public boolean deleteUser(String userId);

  public UserInfo getUserInfo(String userId);

  public UserInfo checkPassword(String userName, String password);

  public UserInfo getUserInfoByName(String userName);
}
