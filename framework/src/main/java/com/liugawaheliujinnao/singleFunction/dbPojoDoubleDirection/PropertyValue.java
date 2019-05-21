package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

/**
 * @Description: Java 属性类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public class PropertyValue {

    private Class propertyClass;

    public Class getPropertyClass() {
        return propertyClass;
    }

    public void setPropertyClass(Class propertyClass) {
        this.propertyClass = propertyClass;
    }

    private PropertyValue() {

    }

    public PropertyValue(Class propertyClass) {
        this.propertyClass = propertyClass;
    }
}
