package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "t_test")
public class TTest implements Serializable {

	private static final long serialVersionUID = -76592615176179102L;

	private Boolean tTestcol22;

	private Long tTestcol3;

	private Integer tTestcol2;

	private Float tTestcol5;

	private Long tTestcol4;

	@Id
	private Long id;

	private Integer tTestcol1;

	private Date tTestcol10;

	private Integer tTestcol;

	private String tTestcol12;

	private String tTestcol13;
}
