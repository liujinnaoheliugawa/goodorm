package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.dao;

import java.util.List;

public interface MemberDao<T, P> {

    List<T> selectByName(String name) throws Exception;

    T selectOne(P id) throws Exception;

    List<T> selectAll() throws Exception;

    boolean insertOne(T m) throws Exception;

    boolean updateOne(T m) throws Exception;

    boolean deleteOne(T m) throws Exception;
}
