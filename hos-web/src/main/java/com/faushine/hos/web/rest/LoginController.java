package com.faushine.hos.web.rest;

import com.google.common.base.Strings;

import com.faushine.hos.core.ErrorCode;
import com.faushine.hos.core.usermgr.model.UserInfo;
import com.faushine.hos.web.security.ContextUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

import javax.servlet.http.HttpSession;

/**
 * @author Yuxin Fan
 * @create 2019-12-24
 */
@Controller
public class LoginController extends BaseController {

  @RequestMapping("/loginPost")
  @ResponseBody
  public Object loginPost(String userName, String password, HttpSession session)throws IOException{
    if (Strings.isNullOrEmpty(userName)|| Strings.isNullOrEmpty(password)){
      return getError(ErrorCode.ERROR_PERMISSOON_DENIED,"error");
    }
    UserInfo userInfo = operationAccessControl.checkLogin(userName,password);
    if (userInfo!=null){
      session.setAttribute(ContextUtil.SESSION_KEY,userInfo.getUserId());
      return getResult("success");
    }else {
      return getError(ErrorCode.ERROR_PERMISSOON_DENIED,"error");
    }
  }

  @RequestMapping("/logout")
  @ResponseBody
  public Object logout(HttpSession session)throws IOException{
    session.removeAttribute(ContextUtil.SESSION_KEY);
    return getResult("success");
  }
}
