package cn.miw.dubbo.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Service;

import cn.miw.dubbo.api.UserService;
import cn.miw.dubbo.model.User;

@Service(version = "1.0")
@Component
public class UserServiceImpl implements UserService {
	
	private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public User findById(Integer id) {
		log.info("接收到的ID：{}",id);
		User user = new User();
		user.setAge(10+id*2);
		user.setName("张三"+id);
		user.setId(id);
		return user;
	}
}
