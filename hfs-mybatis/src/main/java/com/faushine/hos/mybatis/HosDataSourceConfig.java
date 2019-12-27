package com.faushine.hos.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
@Configuration
@MapperScan(basePackages = HosDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "HosSqlSessionFactory")
public class HosDataSourceConfig {

  static final String PACKAGE = "com.faushine.hos.**";

  /**
   * create data source
   */
  @Bean(name = "HosDataSource")
  @Primary
  public DataSource hosDataSource() throws IOException {
    // 1. get data source info
    ResourceLoader loader = new DefaultResourceLoader();
    InputStream inputStream = loader.getResource("classpath:application.properties")
        .getInputStream();
    Properties properties = new Properties();
    properties.load(inputStream);
    Set<Object> keys = properties.keySet();
    Properties newProperties = new Properties();
    for (Object key : keys) {
      if (key.toString().startsWith("datasource")) {
        newProperties.put(key.toString().replace("datasource.", ""), properties.get(key));
      }
    }
    // 2. create a data source from hikariDataSourceFactory
    HikariDataSourceFactory factory = new HikariDataSourceFactory();
    factory.setProperties(newProperties);
    inputStream.close();
    return factory.getDataSource();
  }

  @Bean("HosSqlSessionFactory")
  @Primary
  public SqlSessionFactory hosSqlSessionFactory(
      @Qualifier("HosDataSource") DataSource hosDataSource) throws Exception {
    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
    sqlSessionFactoryBean.setDataSource(hosDataSource);
    // 1. load mybatis properties
    ResourceLoader loader = new DefaultResourceLoader();
    sqlSessionFactoryBean.setConfigLocation(loader.getResource("classpath:mybatis-config.xml"));
    // 2. load instance of SqlSessionFactory
    sqlSessionFactoryBean.setSqlSessionFactoryBuilder(new SqlSessionFactoryBuilder());
    return sqlSessionFactoryBean.getObject();
  }

}
