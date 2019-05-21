package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

import java.lang.reflect.Method;

/**
 * @Description: 动态代理类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public interface GoodOrmInvocationHandler {

    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
