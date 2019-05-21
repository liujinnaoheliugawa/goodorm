package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.dao;

import com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.entity.MyTest;

import java.util.List;

public interface MyTestDao {

	MyTest selectOne(Long id) throws Exception;

	List<MyTest> selectAll() throws Exception;

	boolean insertOne(MyTest m) throws Exception;

	boolean updateOne(MyTest m) throws Exception;

	boolean deleteOne(MyTest m) throws Exception;
}