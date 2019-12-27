package com.faushine.hfs.mybatis;

import com.zaxxer.hikari.HikariDataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public class HikariDataSourceFactory extends UnpooledDataSourceFactory {
  public HikariDataSourceFactory(){
    this.dataSource = new HikariDataSource();
  }
}
