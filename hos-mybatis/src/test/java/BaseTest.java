import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Yuxin Fan
 * @create 2019-12-19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application.properties")
@Component("com.faushine.hos.*")
@MapperScan("com.faushine.hos.*")
public class BaseTest {

}
