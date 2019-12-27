package com.faushine.hfs.server.dao;

import com.faushine.hfs.common.BucketModel;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
@Mapper
public interface BucketModelMapper {
  void addBucket(@Param("bucket")BucketModel bucketModel);
  void deleteBucket(@Param("bucketName")String bucketName);
  void updateBucket(@Param("bucketNmae")String bucketName,@Param("detail")String detail);
  @ResultMap("BucketResultMap")
  BucketModel getBucket(@Param("bucketId") String bucketId);

  @ResultMap("BucketResultMap")
  BucketModel getBucketByName(@Param("bucketName") String bucketName);

  @ResultMap("BucketResultMap")
  List<BucketModel> getBucketByCreator(@Param("creator") String creator);

  @ResultMap("BucketResultMap")
  List<BucketModel> getUserAuthorizedBuckets(@Param("token") String token);
}
