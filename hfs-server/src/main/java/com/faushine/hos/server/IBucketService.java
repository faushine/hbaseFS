package com.faushine.hos.server;

import com.faushine.hos.common.BucketModel;
import com.faushine.hos.core.usermgr.model.UserInfo;

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
