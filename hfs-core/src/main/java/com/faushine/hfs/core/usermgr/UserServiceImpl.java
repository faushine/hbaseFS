package com.faushine.hfs.core.usermgr;

import com.google.common.base.Strings;

import com.faushine.hfs.core.CoreUtil;
import com.faushine.hfs.core.usermgr.dao.UserInfoMapper;
import com.faushine.hfs.core.authmgr.IAuthService;
import com.faushine.hfs.core.authmgr.model.TokenInfo;
import com.faushine.hfs.core.usermgr.model.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
@Transactional
@Service("userServiceImpl")
public class UserServiceImpl implements IUserService {
  private long LONG_REFRESH_TIME = 4670409600000L;
  private int LONG_EXPIRE_TIME = 36500;

  @Autowired
  UserInfoMapper userInfoMapper;

  @Autowired
  @Qualifier("authServiceImpl")
  IAuthService authService;

  @Override
  public boolean addUser(UserInfo userInfo) {
    userInfoMapper.addUser(userInfo);
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setToken(userInfo.getUserId());
    tokenInfo.setActive(true);
    tokenInfo.setExpireTime(LONG_EXPIRE_TIME);
    Date date = new Date();
    tokenInfo.setRefreshTime(date);
    tokenInfo.setCreator(CoreUtil.SYSTEM_USER);
    tokenInfo.setCreateTime(new Date());
    authService.addToken(tokenInfo);
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
    authService.deleteToken(userId);
    authService.deleteAuthByToken(userId);
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
    return userInfoMapper.getUserInfoByName(userName);
  }
}
