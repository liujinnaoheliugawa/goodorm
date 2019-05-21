package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "my_test")
public class MyTest implements Serializable {

	@Id
	private Long myTestId;

	private String thisIsJustTest;

}
