package com.faushine.hfs.core.authmgr;

import com.faushine.hfs.core.authmgr.model.ServiceAuth;
import com.faushine.hfs.core.authmgr.model.TokenInfo;

import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public interface IAuthService {
  public boolean addAuth(ServiceAuth auth);

  public boolean deleteAuth(String bucketName, String token);

  public boolean deleteAuthByBucket(String bucketName);

  public boolean deleteAuthByToken(String token);

  public ServiceAuth getServiceAuth(String bucketName, String token);

  public boolean addToken(TokenInfo tokenInfo);

  public boolean updateToken(String token, int expireTime, boolean isActive);

  public boolean refreshToken(String token);

  public boolean deleteToken(String token);

  public boolean checkToken(String token);

  public TokenInfo getTokenInfo(String token);

  public List<TokenInfo> getTokenInfoList(String creator);
}
