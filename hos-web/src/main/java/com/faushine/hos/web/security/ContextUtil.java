package com.faushine.hos.web.security;

import com.faushine.hos.core.usermgr.model.UserInfo;

/**
 * @author Yuxin Fan
 * @create 2019-12-23
 */
public class ContextUtil {

  public final static String SESSION_KEY = "USER_TOKEN";
  private static ThreadLocal<UserInfo> userInfoThreadLocal = new ThreadLocal<>();

  public static UserInfo getCurrentUser() {
    return userInfoThreadLocal.get();
  }
  public static void setCurrentUser(UserInfo user){
    userInfoThreadLocal.set(user);
  }
  static void clear(){
    userInfoThreadLocal.remove();
  }
}
