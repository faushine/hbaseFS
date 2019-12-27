package com.faushine.hos.web;

import static org.junit.Assert.*;

import com.faushine.hos.server.IHosStore;

import org.junit.Assert;
import org.junit.Test;

import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;

/**
 * @author Yuxin Fan
 * @create 2019-12-24
 */
public class HosServerBeanConfigurationTest {

  @Test
  public void getConnection() throws IOException {
    Connection connection = new HosServerBeanConfiguration().getConnection();
    Assert.assertNotNull(connection);
  }

  @Test
  public void getHosStore() throws Exception {
    HosServerBeanConfiguration configuration = new HosServerBeanConfiguration();
    Connection connection = configuration.getConnection();
    IHosStore hosStore = configuration.getHosStore(connection);
    Assert.assertNotNull(hosStore);
  }
}