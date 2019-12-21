package com.faushine.hos.server.test;

import com.faushine.hos.common.BucketModel;
import com.faushine.hos.core.usermgr.IUserService;
import com.faushine.hos.core.usermgr.model.UserInfo;
import com.faushine.hos.mybatis.test.BaseTest;
import com.faushine.hos.server.IBucketService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class BucketServiceTest extends BaseTest {
  @Autowired
  @Qualifier("bucketServiceImpl")
  IBucketService bucketService;

  @Autowired
  @Qualifier("userServiceImpl")
  IUserService userService;

  @Test
  public void addBucket(){
    UserInfo userInfo = userService.getUserInfoByName("Tom");
    bucketService.addBucket(userInfo,"bucket1","this is a test bucket");
  }

  @Test
  public void getBucket(){
    BucketModel bucketModel = bucketService.getBucketByName("bucket1");
    System.out.println(bucketModel.getCreator());
  }
}
