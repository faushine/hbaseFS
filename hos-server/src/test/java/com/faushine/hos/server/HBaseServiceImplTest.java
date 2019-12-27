package com.faushine.hos.server;

import static org.junit.Assert.*;

import com.faushine.hos.core.HosConfiguration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author Yuxin Fan
 * @create 2019-12-26
 */
@Ignore
public class HBaseServiceImplTest {

  public Connection connection;

  @Before
  public void setup() throws IOException {
    Configuration config = HBaseConfiguration.create();
    HosConfiguration confUtil = HosConfiguration.getConfiguration();
    config.set("hbase.zookeeper.quorum", confUtil.getString("hbase.zookeeper.quorum"));
    config.set("hbase.zookeeper.property.clientPort",
        confUtil.getString("hbase.zookeeper.property.clientPort"));
    config.set(HConstants.HBASE_RPC_TIMEOUT_KEY, "3600000");
    connection = ConnectionFactory.createConnection(config);
  }


  @Test
  public void createTable() {
    HBaseServiceImpl.createTable(connection, HosUtil.BUCKET_DIR_SEQ_TABLE,
        new String[]{HosUtil.BUCKET_DIR_SEQ_CF}, null);
  }
}