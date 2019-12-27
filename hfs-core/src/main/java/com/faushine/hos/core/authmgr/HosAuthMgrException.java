package com.faushine.hos.core.authmgr;

import com.faushine.hos.core.HosException;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public class HosAuthMgrException extends HosException {

  private int code;
  private String message;

  public HosAuthMgrException(int code, String message, Throwable cause) {
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
