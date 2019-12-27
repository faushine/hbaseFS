package com.faushine.hfs.common;

import java.util.Map;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class ObjectMetaData {

  private String bucket;
  private String key;
  private String mediaType;
  private long length;
  private long lastMpdifyTime;

  public String getContentEncoding() {
    return attrs != null ? attrs.get("content-encoding") : null;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  public long getLastMpdifyTime() {
    return lastMpdifyTime;
  }

  public void setLastMpdifyTime(long lastMpdifyTime) {
    this.lastMpdifyTime = lastMpdifyTime;
  }

  public Map<String, String> getAttrs() {
    return attrs;
  }

  public void setAttrs(Map<String, String> attrs) {
    this.attrs = attrs;
  }

  private Map<String, String> attrs;
}
