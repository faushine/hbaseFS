package com.faushine.hos.core.authmgr.model;

import com.faushine.hos.core.CoreUtil;

import java.util.Date;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class TokenInfo {
  private String token;
  private int expireTime;
  private Date refreshTime;
  private Date createTime;
  private boolean isActive;
  private String creator;

  public TokenInfo(){}

  public TokenInfo(String creator) {
    this.token = CoreUtil.getUUIDStr();
    this.expireTime = 7;
    Date now = new Date();
    this.refreshTime = now;
    this.createTime = now;
    this.isActive = true;
    this.creator = creator;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public int getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(int expireTime) {
    this.expireTime = expireTime;
  }

  public Date getRefreshTime() {
    return refreshTime;
  }

  public void setRefreshTime(Date refreshTime) {
    this.refreshTime = refreshTime;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

}
