package com.faushine.hfs.core;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public abstract class HosException extends RuntimeException {
  protected String errorMessage;
  public HosException(String message, Throwable cause){
    super(cause);
    this.errorMessage = message;
  }

  public abstract int errorCode();

  public String errorMessage(){
    return this.errorMessage;
  }
}
