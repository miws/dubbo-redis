package cn.miw.dubbo.api;

import cn.miw.dubbo.model.User;

public interface UserService {
	User findById(Integer id);
}
