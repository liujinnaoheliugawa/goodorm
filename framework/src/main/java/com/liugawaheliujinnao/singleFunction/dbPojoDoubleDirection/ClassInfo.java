package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

import java.util.List;

/**
 * @Description: 类文件信息类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public class ClassInfo {

    private String src;

    private List<String> unNullableFields;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<String> getUnNullableFields() {
        return unNullableFields;
    }

    public void setUnNullableFields(List<String> unNullableFields) {
        this.unNullableFields = unNullableFields;
    }
}
