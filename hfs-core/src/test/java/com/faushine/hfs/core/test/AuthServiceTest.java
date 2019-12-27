package com.faushine.hfs.core.test;

import com.faushine.hfs.core.authmgr.IAuthService;
import com.faushine.hfs.core.authmgr.model.ServiceAuth;
import com.faushine.hfs.core.authmgr.model.TokenInfo;
import com.faushine.hfs.mybatis.test.BaseTest;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
@Ignore
public class AuthServiceTest extends BaseTest {
  @Autowired
  @Qualifier("authServiceImpl")
  IAuthService authService;

  @Test
  public void addToken(){
    TokenInfo tokenInfo = new TokenInfo("Tom");
    authService.addToken(tokenInfo);
  }

  @Test
  public void getTokenByUser(){
    List<TokenInfo> tokenInfoList = authService.getTokenInfoList("Tom");
    Assert.assertTrue(tokenInfoList.size()>0);
  }

  @Test
  public void addServiceAuth(){
    ServiceAuth serviceAuth = new ServiceAuth();
    serviceAuth.setBucketName("bucket1");
    serviceAuth.setTargetToken("2660dc3fdc564efbb216b7ff4b35d0ca");
    serviceAuth.setAuthTime(new Date());
    Assert.assertTrue(authService.addAuth(serviceAuth));
  }

  @Test
  public void getServiceAuth(){
    ServiceAuth serviceAuth = authService.getServiceAuth("bucket1","2660dc3fdc564efbb216b7ff4b35d0ca");
    System.out.println(serviceAuth.getBucketName());
  }
}
