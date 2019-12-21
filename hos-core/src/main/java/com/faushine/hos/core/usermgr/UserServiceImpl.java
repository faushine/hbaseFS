package com.faushine.hos.core.usermgr;

import com.google.common.base.Strings;

import com.faushine.hos.core.CoreUtil;
import com.faushine.hos.core.usermgr.model.UserInfo;
import com.faushine.hos.core.usermgr.dao.UserInfoMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
@Transactional
@Service("userServiceImpl")
public class UserServiceImpl implements IUserService {

  @Autowired
  UserInfoMapper userInfoMapper;

  @Override
  public boolean addUser(UserInfo userInfo) {
    userInfoMapper.addUser(userInfo);
    // todo:add token
    return true;
  }

  @Override
  public boolean updateUserInfo(String userId, String password, String detail) {
    userInfoMapper.updateUserInfo(userId,
        Strings.isNullOrEmpty(password) ? null : CoreUtil.getMd5Password(password),
        Strings.emptyToNull(detail));
    return true;
  }

  @Override
  public boolean deleteUser(String userId) {
    userInfoMapper.deleteUser(userId);
    // todo: delete token and auth
    return true;
  }

  @Override
  public UserInfo getUserInfo(String userId) {
    return userInfoMapper.getUserInfo(userId);
  }

  @Override
  public UserInfo checkPassword(String userName, String password) {
    return userInfoMapper.checkPassword(userName,CoreUtil.getMd5Password(password));
  }

  @Override
  public UserInfo getUserInfoByName(String userName) {
    return null;
  }
}
