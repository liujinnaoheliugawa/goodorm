package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "t_member")
public class TMember implements Serializable {

	private Long createTime;

	private String name;

	@Id
	private Long id;

	private String addr;

}
