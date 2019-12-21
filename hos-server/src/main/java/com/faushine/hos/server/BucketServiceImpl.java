package com.faushine.hos.server;

import com.faushine.hos.common.BucketModel;
import com.faushine.hos.core.authmgr.IAuthService;
import com.faushine.hos.core.authmgr.model.ServiceAuth;
import com.faushine.hos.core.usermgr.model.UserInfo;
import com.faushine.hos.server.dao.BucketModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
@Transactional
@Service("bucketServiceImpl")
public class BucketServiceImpl implements IBucketService {

  @Autowired
  BucketModelMapper bucketModelMapper;

  @Autowired
  @Qualifier("authServiceImpl")
  IAuthService authService;

  @Override
  public boolean addBucket(UserInfo userInfo, String bucketName, String detail) {
    BucketModel bucketModel = new BucketModel(bucketName,userInfo.getUserName(),detail);
    bucketModelMapper.addBucket(bucketModel);
    //todo add auth for bucket and user
    ServiceAuth serviceAuth = new ServiceAuth();
    serviceAuth.setAuthTime(new Date());
    serviceAuth.setTargetToken(userInfo.getUserId());
    serviceAuth.setBucketName(bucketName);
    authService.addAuth(serviceAuth);
    return true;
  }

  @Override
  public boolean deleteBucket(String bucketName) {
    bucketModelMapper.deleteBucket(bucketName);
    //todo delete auth for bucket
    authService.deleteAuthByBucket(bucketName);
    return true;
  }

  @Override
  public boolean updateBucket(String bucketName, String detail) {
    bucketModelMapper.updateBucket(bucketName,detail);
    return true;
  }

  @Override
  public BucketModel getBucketById(String bucketId) {
    return bucketModelMapper.getBucket(bucketId);
  }

  @Override
  public BucketModel getBucketByName(String bucketName) {
    return bucketModelMapper.getBucketByName(bucketName);
  }

  @Override
  public List<BucketModel> getBucketByCreator(String creator) {
    return bucketModelMapper.getBucketByCreator(creator);
  }

  @Override
  public List<BucketModel> getUserBuckets(String token) {
    return bucketModelMapper.getUserAuthorizedBuckets(token);
  }
}
