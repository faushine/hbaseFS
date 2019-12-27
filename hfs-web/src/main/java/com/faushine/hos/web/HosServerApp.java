package com.faushine.hos.web;

import com.faushine.hos.mybatis.HosDataSourceConfig;
import com.faushine.hos.web.security.SecurityInterceptor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Yuxin Fan
 * @create 2019-12-24
 */
@EnableWebMvc
@SuppressWarnings("deprecation")
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
@Configuration
@ComponentScan({"com.faushine.*"})
@SpringBootApplication
@Import({HosDataSourceConfig.class, HosServerBeanConfiguration.class})
@MapperScan("com.faushine.hos")
public class HosServerApp {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private SecurityInterceptor securityInterceptor;

  public static void main(String[] args) {
    SpringApplication.run(HosServerApp.class);
  }
  @Bean
  public WebMvcConfigurer configurer(){
    return new WebMvcConfigurerAdapter() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*");
      }

      @Override
      public void addInterceptors(InterceptorRegistry registration) {
        registration.addInterceptor(securityInterceptor);
      }
    };
  }
}
