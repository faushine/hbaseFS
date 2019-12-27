package com.faushine.hfs.core.authmgr.model;

import java.util.Date;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class ServiceAuth {
  private String bucketName;
  private String targetToken;
  private Date authTime;

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getTargetToken() {
    return targetToken;
  }

  public void setTargetToken(String targetToken) {
    this.targetToken = targetToken;
  }

  public Date getAuthTime() {
    return authTime;
  }

  public void setAuthTime(Date authTime) {
    this.authTime = authTime;
  }
}
