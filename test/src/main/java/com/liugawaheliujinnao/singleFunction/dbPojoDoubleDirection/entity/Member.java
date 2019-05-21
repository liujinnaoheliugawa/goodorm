package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name="member")
public class Member implements Serializable {

    private static final long serialVersionUID = 1636127612726799748L;

    @Id
    private Long id;
    private String name;
    private String addr;
    private Long createTime;
}
