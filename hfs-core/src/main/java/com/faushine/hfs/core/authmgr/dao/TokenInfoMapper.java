package com.faushine.hfs.core.authmgr.dao;

import com.faushine.hfs.core.authmgr.model.TokenInfo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

import java.util.Date;
import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
@Mapper
public interface TokenInfoMapper {

  public void addToken(@Param("token") TokenInfo tokenInfo);

  public void deleteToken(@Param("token") String token);

  public void updateToken(@Param("token") String token, @Param("expireTime") int expireTime,@Param("isActive") int isActive);

  public void refreshToken(@Param("token") String token, @Param("refreshTime") Date refreshTime);

  @ResultMap("TokenInfoResultMap")
  public TokenInfo getTokenInfo(@Param("token") String token);


  @ResultMap("TokenInfoResultMap")
  public List<TokenInfo> getTokenInfoList(@Param("creator") String creator);
}
