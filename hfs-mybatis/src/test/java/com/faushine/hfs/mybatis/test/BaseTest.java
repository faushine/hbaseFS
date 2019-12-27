package com.faushine.hfs.mybatis.test;

import com.faushine.hfs.mybatis.HosDataSourceConfig;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@Import(HosDataSourceConfig.class)
@PropertySource("classpath:application.properties")
@ComponentScan("com.faushine.*")
@MapperScan("com.faushine.*")
public class BaseTest {

}
