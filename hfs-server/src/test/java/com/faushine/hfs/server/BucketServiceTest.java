package com.faushine.hfs.server;

import com.faushine.hfs.common.BucketModel;
import com.faushine.hfs.core.usermgr.IUserService;
import com.faushine.hfs.core.usermgr.model.UserInfo;
import com.faushine.hfs.mybatis.test.BaseTest;
import com.faushine.hfs.server.IBucketService;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
@Ignore
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
