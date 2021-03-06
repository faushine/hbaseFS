package com.faushine.hfs.web.rest;

import com.faushine.hfs.core.ErrorCode;
import com.faushine.hfs.core.authmgr.IAuthService;
import com.faushine.hfs.core.authmgr.model.ServiceAuth;
import com.faushine.hfs.core.authmgr.model.TokenInfo;
import com.faushine.hfs.core.usermgr.IUserService;
import com.faushine.hfs.core.usermgr.model.SystemRole;
import com.faushine.hfs.core.usermgr.model.UserInfo;
import com.faushine.hfs.web.security.ContextUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-24
 */
@RestController
@RequestMapping("hos/v1/sys")
public class ManageController extends BaseController {
  @Autowired
  @Qualifier("authServiceImpl")
  IAuthService authService;

  @Autowired
  @Qualifier("userServiceImpl")
  IUserService userService;

  @RequestMapping(value = "user", method = RequestMethod.POST)
  public Object createUser(@RequestParam("userName") String userName,
      @RequestParam("password") String password,
      @RequestParam(name = "detail", required = false, defaultValue = "") String detail,
      @RequestParam(name = "role", required = false, defaultValue = "USER") String role) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl
        .checkSystemRole(currentUser.getSystemRole(), SystemRole.valueOf(role))) {
      UserInfo userInfo = new UserInfo(userName, password, detail,SystemRole.valueOf(role));
      userService.addUser(userInfo);
      return getResult("success");
    }
    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "NOT ADMIN");
  }

  @RequestMapping(value = "user", method = RequestMethod.DELETE)
  public Object deleteUser(@RequestParam("userId") String userId) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkSystemRole(currentUser.getSystemRole(), userId)) {
      userService.deleteUser(userId);
      return getResult("success");
    }
    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");
  }

  @RequestMapping(value = "user", method = RequestMethod.PUT)
  public Object updateUserInfo(
      @RequestParam(name = "password", required = false, defaultValue = "") String password,
      @RequestParam(name = "detail", required = false, defaultValue = "") String detail) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (currentUser.getSystemRole().equals(SystemRole.VISITOR)) {
      return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");
    }

    userService.updateUserInfo(currentUser.getUserId(), password, detail);
    return getResult("success");
  }

  @RequestMapping(value = "user", method = RequestMethod.GET)
  public Object getUserInfo() {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    return getResult(currentUser);
  }

  @RequestMapping(value = "token", method = RequestMethod.POST)
  public Object createToken(
      @RequestParam(name = "expireTime", required = false, defaultValue = "7") String expireTime,
      @RequestParam(name = "isActive", required = false, defaultValue = "true") String isActive) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!currentUser.getSystemRole().equals(SystemRole.VISITOR)) {
      TokenInfo tokenInfo = new TokenInfo(currentUser.getUserName());
      tokenInfo.setExpireTime(Integer.parseInt(expireTime));
      tokenInfo.setActive(Boolean.parseBoolean(isActive));
      authService.addToken(tokenInfo);
      return getResult(tokenInfo);
    }
    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "NOT USER");
  }

  @RequestMapping(value = "token", method = RequestMethod.DELETE)
  public Object deleteToken(@RequestParam("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      authService.deleteToken(token);
      return getResult("success");
    }
    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");
  }

  @RequestMapping(value = "token", method = RequestMethod.PUT)
  public Object updateTokenInfo(
      @RequestParam("token") String token,
      @RequestParam(name = "expireTime", required = false, defaultValue = "7") String expireTime,
      @RequestParam(name = "isActive", required = false, defaultValue = "true") String isActive) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      authService.updateToken(token, Integer.parseInt(expireTime), Boolean.parseBoolean(isActive));
      return getResult("success");
    }

    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");
  }

  @RequestMapping(value = "token", method = RequestMethod.GET)
  public Object getTokenInfo(@RequestParam("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      TokenInfo tokenInfo = authService.getTokenInfo(token);
      return getResult(tokenInfo);
    }

    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");

  }

  @RequestMapping(value = "token/list", method = RequestMethod.GET)
  public Object getTokenInfoList() {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!currentUser.getSystemRole().equals(SystemRole.VISITOR)) {
      List<TokenInfo> tokenInfos = authService.getTokenInfoList(currentUser.getUserName());
      return getResult(tokenInfos);
    }

    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");

  }

  @RequestMapping(value = "token/refresh", method = RequestMethod.POST)
  public Object refreshToken(@RequestParam("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkTokenOwner(currentUser.getUserName(), token)) {
      authService.refreshToken(token);
      return getResult("success");
    }

    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");
  }

  @RequestMapping(value = "auth", method = RequestMethod.POST)
  public Object createAuth(@RequestBody ServiceAuth serviceAuth) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl
        .checkBucketOwner(currentUser.getUserName(), serviceAuth.getBucketName())
        && operationAccessControl
        .checkTokenOwner(currentUser.getUserName(), serviceAuth.getTargetToken())) {
      authService.addAuth(serviceAuth);
      return getResult("success");
    }
    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");
  }

  @RequestMapping(value = "auth", method = RequestMethod.DELETE)
  public Object deleteAuth(@RequestParam("bucket") String bucket,
      @RequestParam("token") String token) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl
        .checkBucketOwner(currentUser.getUserName(), bucket)
        && operationAccessControl
        .checkTokenOwner(currentUser.getUserName(), token)) {
      authService.deleteAuth(bucket, token);
      return getResult("success");
    }
    return getError(ErrorCode.ERROR_PERMISSOON_DENIED, "PERMISSION DENIED");
  }

}
