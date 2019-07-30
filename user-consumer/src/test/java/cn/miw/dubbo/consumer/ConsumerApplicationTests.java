package cn.miw.dubbo.consumer;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import cn.miw.dubbo.model.User;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsumerApplicationTests {

	@Test
	public void contextLoads() {
		RestTemplate restTemplate=new RestTemplate();
		User user = restTemplate.getForObject("http://localhost:8080?id={1}", User.class,100);
		System.out.println(user);
	}

}
