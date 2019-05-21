package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description: 测试类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public class Test {

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
        MysqlDatabaseConnection connection = (MysqlDatabaseConnection)ctx.getBean("mysqlDatabaseConnection");
//        TableUtils.generateJava(connection, Test.class);
        TableUtils.generateTable(connection, Test.class);
    }
}
