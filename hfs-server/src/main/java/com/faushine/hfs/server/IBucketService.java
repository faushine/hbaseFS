package com.faushine.hfs.server;

import com.faushine.hfs.common.BucketModel;
import com.faushine.hfs.core.usermgr.model.UserInfo;

import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public interface IBucketService {

  public boolean addBucket(UserInfo userInfo, String bucketName, String detail);

  public boolean deleteBucket(String bucketName);

  public boolean updateBucket(String bucketName, String detail);

  public BucketModel getBucketById(String bucketId);

  public BucketModel getBucketByName(String bucketName);

  public List<BucketModel> getBucketByCreator(String creator);

  public List<BucketModel> getUserBuckets(String token);
}
