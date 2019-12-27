package com.faushine.hos.core;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public interface ErrorCode {
  public static final int ERROR_PERMISSOON_DENIED = 403;
  public static final int ERROR_FILE_NOT_FOUND = 404;
  public static final int ERROR_HBASE = 500;
  public static final int ERROR_HDFS = 501;
}
