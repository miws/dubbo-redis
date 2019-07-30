package cn.miw.dubbo.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;

import cn.miw.dubbo.api.UserService;
import cn.miw.dubbo.model.User;

@SpringBootApplication
@EnableDubbo
@RestController
public class ConsumerApp {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApp.class, args);
	}
	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Reference(version = "1.0")
	UserService userService;
	
	private static Logger log = LoggerFactory.getLogger(ConsumerApp.class);
	
	@GetMapping("/")
	public Object index(Integer id) {
		log.info("输入的ID：{},{}",id,(id==null)?0:id);
		return userService.findById((id==null)?0:id);
	}
	@Autowired
	RestTemplate restTemplate;
	@GetMapping("/rest")
	public Object rest(Integer id) {
		User user = restTemplate.getForObject("http://localhost:8080?id={1}", User.class,id);
		return user;
	}
}
