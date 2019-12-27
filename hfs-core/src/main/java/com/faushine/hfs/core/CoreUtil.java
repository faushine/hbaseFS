package com.faushine.hfs.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public class CoreUtil {

  public final static String SYSTEM_USER = "SuperAdmin";

  public static String getUUIDStr(){
    return UUID.randomUUID().toString().replace("-", "");
  }

  public static String getMd5Password(String str) {
    String reStr = null;
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] bytes = md5.digest(str.getBytes());
      StringBuffer stringBuffer = new StringBuffer();
      for (byte b : bytes) {
        int bt = b & 0xff;
        if (bt < 16) {
          stringBuffer.append(0);
        }
        stringBuffer.append(Integer.toHexString(bt));
      }
      reStr = stringBuffer.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return reStr;
  }


}
