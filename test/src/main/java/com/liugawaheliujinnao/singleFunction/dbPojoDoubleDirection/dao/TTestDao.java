package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.dao;

import com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.entity.TTest;

import java.util.List;

public interface TTestDao {

	TTest selectOne(Long id) throws Exception;

	List<TTest> selectAll() throws Exception;

	boolean insertOne(TTest m) throws Exception;

	boolean updateOne(TTest m) throws Exception;

	boolean deleteOne(TTest m) throws Exception;
}