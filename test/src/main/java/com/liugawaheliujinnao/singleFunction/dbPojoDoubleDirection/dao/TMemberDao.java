package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.dao;


import com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.entity.TMember;

import java.util.List;

public interface TMemberDao {

	TMember selectOne(Long id) throws Exception;

	List<TMember> selectAll() throws Exception;

	boolean insertOne(TMember m) throws Exception;

	boolean updateOne(TMember m) throws Exception;

	boolean deleteOne(TMember m) throws Exception;
}