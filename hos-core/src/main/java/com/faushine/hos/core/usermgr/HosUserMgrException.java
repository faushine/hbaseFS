package com.faushine.hos.core.usermgr;

import com.faushine.hos.core.HosException;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public class HosUserMgrException extends HosException {

  private int code;
  private String message;

  public HosUserMgrException(int code, String message, Throwable cause) {
    super(message, null);
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int errorCode() {
    return this.code;
  }
}
