package com.faushine.hos.server;

import com.faushine.hos.core.HosException;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class HosServerException extends HosException {

  private int code;
  private String message;

  public HosServerException(int code, String message) {
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
