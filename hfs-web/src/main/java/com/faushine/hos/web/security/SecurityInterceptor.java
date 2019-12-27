package com.faushine.hos.web.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.faushine.hos.core.authmgr.IAuthService;
import com.faushine.hos.core.authmgr.model.TokenInfo;
import com.faushine.hos.core.usermgr.IUserService;
import com.faushine.hos.core.usermgr.model.SystemRole;
import com.faushine.hos.core.usermgr.model.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Yuxin Fan
 * @create 2019-12-23
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {

  @Autowired
  @Qualifier("authServiceImpl")
  private IAuthService authService;

  @Autowired
  @Qualifier("userServiceImpl")
  private IUserService userService;

  private Cache<String, UserInfo> userInfoCache = CacheBuilder.newBuilder()
      .expireAfterWrite(20, TimeUnit.MINUTES).build();

  @Override
  public boolean preHandle(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o) throws Exception {
    if (httpServletRequest.getRequestURI().endsWith("/loginPost")){
      return true;
    }
    String token = "";
    HttpSession session = httpServletRequest.getSession();
    if (session.getAttribute(ContextUtil.SESSION_KEY)!=null){
      token = session.getAttribute(ContextUtil.SESSION_KEY).toString();
    }else {
      token = httpServletRequest.getHeader("X-Auth-Token");
    }
    TokenInfo tokenInfo = authService.getTokenInfo(token);
    if (tokenInfo==null){
      String url = "/loginPost";
      httpServletResponse.sendRedirect(url);
      return false;
    }
    UserInfo userInfo = userInfoCache.getIfPresent(tokenInfo.getToken());
    if (userInfo == null) {
      userInfo = userService.getUserInfo(token);
      if (userInfo == null) {
        userInfo = new UserInfo();
        userInfo.setUserId(token);
        userInfo.setUserName("NOT_EXIST_USER");
        userInfo.setDetail("a temporary visitor");
        userInfo.setSystemRole(SystemRole.VISITOR);
      }
      userInfoCache.put(tokenInfo.getToken(), userInfo);
    }
    ContextUtil.setCurrentUser(userInfo);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView)
      throws Exception {

  }

  @Override
  public void afterCompletion(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

  }
}
