package com.faushine.hfs.core;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
public class HosConfiguration {

  private static HosConfiguration configuration;
  private static Properties properties;

  static {

    PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
    configuration = new HosConfiguration();
    try {
      configuration.properties = new Properties();
      Resource[] resources = resourceLoader.getResources("classpath:*.properties");
      for (Resource resource : resources) {
        Properties props = new Properties();
        InputStream input = resource.getInputStream();
        props.load(input);
        input.close();
        configuration.properties.putAll(props);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private HosConfiguration() {
  }

  public static HosConfiguration getConfiguration() {
    return configuration;
  }

  public String getString(String key) {
    return properties.get(key).toString();
  }

  public int getInt(String key) {
    return Integer.parseInt(properties.getProperty(key));
  }
}
