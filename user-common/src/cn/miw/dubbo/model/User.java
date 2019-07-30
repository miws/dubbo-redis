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
