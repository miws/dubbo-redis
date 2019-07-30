# dubbo-redis
使用redis作为dubbo注册中心调用样例


---
title: 使用Redis为注册中心的Dubbo微服务架构（基于SpringBoot）
date: 2019-07-30 14:06:29
categories: 
  - 架构
author: mrzhou
tags: 
  - SpringBoot
  - redis
  - Dubbo
  - 微服务
  - RPC
---

### 前言

Dubbo作为一款高性能的RPC框架，已经在大多数一线IT企业中得到了广泛的使用，今天我们也来试一试。为了尽可能的少写代码，让程序简单明了，我们基于SpringBoot来搭建。
虽然Dubbo推荐使用zk作为注册中心，今天我们换换Redis试试。

### 需求分解

本项目只是为了讲解Dubbo与Redis的使用，所以将业务尽可能地简化，我们的消费方调用服务提供方获取一个简单的用户信息。
将业务进行分解：
* 接口定义(user-common)
* 服务提供方(user-provider)
* 服务消费方(user-consumer)

### 接口项目

原则上该项目其实只是接口定义部分就可以了，其实什么依赖都可以不需要，只是为了少写代码，这里引入了lombok，然后将该项目打包发布到maven仓库即可。
pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>cn.miw.dubbo</groupId>
	<artifactId>common</artifactId>
	<version>0.0.1</version>
	<dependencies>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>1.18.8</version>
			<scope>provided</scope>
		</dependency>

	</dependencies>
	<build>
		<sourceDirectory>src</sourceDirectory>
		<plugins>
			<plugin>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.0</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```

接口定义
UserService.java

```java
package cn.miw.dubbo.api;

import cn.miw.dubbo.model.User;

public interface UserService {
	User findById(Integer id);
}
```

实体定义
User.java

```java
package cn.miw.dubbo.model;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class User implements Serializable{

	private static final long serialVersionUID = -7001216319830050312L;
	private Integer id;
	private String name;
	private int age;
	
}

```

### 服务提供方项目

在该项目中实现具体的服务处理逻辑并完成注册中心注册的过程，该项目可以集成一些成熟的orm框架，这里为了简单演示，其实现只是简单地创建一个对象返回即可。
* pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.1.RELEASE</version>
		<relativePath />
	</parent>
	<groupId>cn.miw.dubbo</groupId>
	<artifactId>user-provider</artifactId>
	<version>0.0.1</version>
	<packaging>jar</packaging>
	<name>demo</name>
	<description>server</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<!-- 引入通用项目 -->
		<dependency>
			<groupId>cn.miw.dubbo</groupId>
			<artifactId>common</artifactId>
			<version>0.0.1</version>
		</dependency>
		<!-- Dubbo需要的第三方包 -->
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-pool2</artifactId>
		</dependency>
		<!-- Redis 支持包 -->
		<dependency>
			<groupId>redis.clients</groupId>
			<artifactId>jedis</artifactId>
		</dependency>
		<!-- Dubbo starter -->
		<dependency>
			<groupId>com.alibaba.boot</groupId>
			<artifactId>dubbo-spring-boot-starter</artifactId>
			<version>0.2.0</version>
		</dependency>
		<!-- Redis starter -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
		</dependency>
		
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>

```

这是一个SpringBoot项目，我们需要使用redis作为注册中心，所以引入了相关的starter及支持包，再加入Dubbo的starter和支持包。

先看看功能的具体简单实现,这里需要注意的是@Service注解，这是使用的dubbo中的注解,通过这个注解即实现了具体服务层在注册中心的流程过程，所以还需要@Component来加持，让其能够被Spring管理。

* UserServiceImpl.java

```java
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
```

再看看启动类，也非常简单，只是增加了@EnableDubbo注解即可，由于该项目并非web项目，所以增加了一句```java System.in.read();```，让其保持运行状态。
* ServerApp.java

```java
package cn.miw.dubbo.server;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;

@SpringBootApplication
@EnableDubbo
public class ServerApp {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(ServerApp.class, args);
		System.in.read();
	}

}
```

再来看看配置。这里指出了注册中心redis的位置，并指明了提供服务所使用的协议为dubbo协议，服务端口为:12345
* application.yml

```yml

dubbo:
  application:
    id: user-provider
    name: user-provider    
    qosEnable: true
    qosPort: 22223
  registry:
    address: redis://root:pass@192.168.0.116:6379

  protocol:
    name: dubbo
    port: 12345    
    
spring:
  application:
    name: user-provider    
```

### 服务调用方项目

这个项目的依赖仅比服务提供方多了一个web项目的依赖
* pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.1.RELEASE</version>
		<relativePath />
	</parent>
	<groupId>cn.miw.dubbo</groupId>
	<artifactId>consumer</artifactId>
	<version>0.0.1</version>
	<packaging>war</packaging>
	<name>user-consumer</name>
	<description>consumer</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<!-- Dubbo及redis通用部分 开始 -->
		<dependency>
			<groupId>cn.miw.dubbo</groupId>
			<artifactId>common</artifactId>
			<version>0.0.1</version>
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-pool2</artifactId>
		</dependency>
		<dependency>
			<groupId>redis.clients</groupId>
			<artifactId>jedis</artifactId>
		</dependency>
		<dependency>
			<groupId>com.alibaba.boot</groupId>
			<artifactId>dubbo-spring-boot-starter</artifactId>
			<version>0.2.0</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
		</dependency>
		<!-- Dubbo及redis通用部分 结束 -->

		<!-- Web项目 -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>

```

为了尽可能简单实现我们只有一个java文件
* ConsumerApp.java

```java
package cn.miw.dubbo.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;

import cn.miw.dubbo.api.UserService;

@SpringBootApplication
@EnableDubbo
@RestController
public class ConsumerApp {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApp.class, args);
	}

	@Reference(version = "1.0")
	UserService userService;
	
	private static Logger log = LoggerFactory.getLogger(ConsumerApp.class);
	
	@GetMapping("/")
	public Object index(Integer id) {
		log.info("输入的ID：{},{}",id,(id==null)?0:id);
		return userService.findById((id==null)?0:id);
	}
}

```

配置文件
* application.yml

```yml
dubbo:
  application:
    id: user-consumer
    name: user-consumer        
    qosEnable: false
    qosPort: 22224
  registry:
    address: redis://root:pass@192.168.0.116:6379
  consumer:
    timeout: 3000

    
spring:
  application:
    name: user-consumer    
```

### 注意事项

* 由于common项目是其他两个项目都需要使用的，请先编译发布到自己的maven仓库，如果都在同一台电脑上开发，只需要install到本地仓库即可，如果是多台电脑或者小组开发请发布到自己小组的私有仓库；
* 在启动服务消费方前请先启动服务提供方，可以多台电脑同时运行服务提供方项目；
* 启动消费方 通过浏览器访问 http://localhost:8080/ 查看结果。也可以启动多个消费方，看看在多个服务方运行的情况下分别调用的是哪一台的服务。

### 结语

这样拆分后，各其实其实都很简单，具体开发中，各项目只需要处理自己相关业务代码即可，不需要考虑更多，可以让每一个项目都可以完成得更好，并且不影响其他模块的功能，让开发和部署维护都能够更方便。
