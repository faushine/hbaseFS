package com.faushine.hos.web;

import com.faushine.hos.core.HosConfiguration;
import com.faushine.hos.server.HdfsServiceImpl;
import com.faushine.hos.server.HosStoreImpl;
import com.faushine.hos.server.IHosStore;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Yuxin Fan
 * @create 2019-12-23
 */
@Configuration
public class HosServerBeanConfiguration {
  // get hbase connection
  @Bean
  public Connection getConnection()throws IOException{
    org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
    HosConfiguration confUtil = HosConfiguration.getConfiguration();
    config.set("hbase.zookeeper.quorum", confUtil.getString("hbase.zookeeper.quorum"));
    config.set("hbase.zookeeper.property.clientPort",
        confUtil.getString("hbase.zookeeper.property.clientPort"));
    config.set(HConstants.HBASE_RPC_TIMEOUT_KEY, "3600000");
    return ConnectionFactory.createConnection(config);
  }
  // create a hosStore instance
  @Bean(name = "hosStore")
  public IHosStore getHosStore(@Autowired Connection connection)throws Exception{
    HosConfiguration hosConfiguration = HosConfiguration.getConfiguration();
    String zkHosts = hosConfiguration.getString("hbase.zookeeper.quorum");
    HdfsServiceImpl hdfsService = new HdfsServiceImpl();
    HosStoreImpl hosStore = new HosStoreImpl(connection,hdfsService,zkHosts);
    return hosStore;
  }
}
