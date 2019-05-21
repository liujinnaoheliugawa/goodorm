package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: Mysql 字段
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MySqlColumn {

    boolean notNull() default false;
    boolean isUnique() default false;
    boolean unsigned() default false;
    boolean zeroFill() default false;
    boolean autoIncrement() default false;
    String length() default "";
    String defaultValue() default "";
}
