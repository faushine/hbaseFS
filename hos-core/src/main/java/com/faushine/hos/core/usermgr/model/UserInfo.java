package com.faushine.hos.core.usermgr.model;

import com.faushine.hos.core.CoreUtil;

import java.util.Date;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public class UserInfo {
  private String userId;
  private String userName;
  private String password;
  private String detail;
  private SystemRole systemRole;
  private Date createTime;

  public UserInfo(){}

  public UserInfo(String userName, String password, String detail, SystemRole systemRole) {
    this.userId = CoreUtil.getUUIDStr();
    this.userName = userName;
    this.password = CoreUtil.getMd5Password(password);
    this.detail = detail;
    this.systemRole = systemRole;
    this.createTime = new Date();
  }


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public SystemRole getSystemRole() {
    return systemRole;
  }

  public void setSystemRole(SystemRole systemRole) {
    this.systemRole = systemRole;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
}
