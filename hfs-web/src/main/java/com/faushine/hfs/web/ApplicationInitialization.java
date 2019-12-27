package com.faushine.hfs.web;

import com.faushine.hfs.core.CoreUtil;
import com.faushine.hfs.core.usermgr.IUserService;
import com.faushine.hfs.core.usermgr.model.SystemRole;
import com.faushine.hfs.core.usermgr.model.UserInfo;
import com.faushine.hfs.server.IHosStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Yuxin Fan
 * @create 2019-12-23
 */
@Component
public class ApplicationInitialization implements ApplicationRunner {


  @Autowired
  @Qualifier("userServiceImpl")
  IUserService userService;

  @Autowired
  @Qualifier("hosStore")
  IHosStore hosStore;

  @Override
  public void run(ApplicationArguments applicationArguments) throws Exception {
    // create admin account
    UserInfo userInfo = userService.getUserInfoByName(CoreUtil.SYSTEM_USER);
    if (userInfo==null){
      UserInfo admin = new UserInfo(CoreUtil.SYSTEM_USER,"superadmin","this is a super admin",
          SystemRole.SUPERADMIN);
      userService.addUser(admin);
    }
    // create seqId table
    hosStore.createSeqTable();
  }
}
