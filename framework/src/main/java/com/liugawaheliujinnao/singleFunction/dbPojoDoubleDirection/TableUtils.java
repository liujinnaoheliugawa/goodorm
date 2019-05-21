package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

import com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @Description: 表格工具类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public class TableUtils {

    public static final String ln = "\r\n";

    public static void generateJava(MysqlDatabaseConnection connection, Class templateClazz) throws Exception {
        String schema = connection.getSchema();
        String packageName = templateClazz.getPackage().getName();
        boolean copy = packageName.contains("entity");
        packageName = packageName.contains("entity") ? packageName : templateClazz.getPackage().getName() + ".entity";
        try {
            if (templateClazz != null) {
                Connection conn = getConnection(connection);
                DatabaseMetaData dbmd = conn.getMetaData();
                ResultSet tableRs = dbmd.getTables(schema, "%", "%", new String[]{"TABLE"});
                while (tableRs.next()) {
                    String tableName = tableRs.getString("TABLE_NAME");
                    String className = convertTableNameToClassName(tableName);
                    String fullName = packageName + "." + className;
                    String keyType = "";
                    ResultSet columnRs = dbmd.getColumns(schema, "%", tableName, "%");
                    try {
                        Class.forName(fullName);
                    } catch (ClassNotFoundException e) {
                        //pojo类不存在，新建

                        //表字段转Java类型
                        Map<String, String> propertyMap = new HashMap<String, String>();
                        //约定优于配置，约定只能有一个id列
                        ResultSet keySet = dbmd.getPrimaryKeys(conn.getCatalog().toUpperCase(), null, tableName.toUpperCase());
                        String keyProperty = "";
                        String keyColumn = "";
                        while (keySet.next()) {
                            keyColumn = keySet.getString("COLUMN_NAME");
                            keyProperty = convertColumnNameToPropertyName(keyColumn);
                        }
                        while (columnRs.next()) {
                            String columnName = columnRs.getString("COLUMN_NAME");
                            String columnType = columnRs.getString("TYPE_NAME");
                            if (keyColumn.equals(columnName)) {
                                keyType = convertJavaType(columnType);
                            }
                            propertyMap.put(columnName, columnType);
                        }

                        propertyMap = convertColumnMapToPropertyMap(propertyMap);

                        //生成Java文件
                        String src = generateSrc(packageName, className, tableName, keyProperty, templateClazz, propertyMap, copy);
                        String filePath = new File(templateClazz.getResource("").getPath()).getPath();
                        filePath = filePath.replace("/target/classes", "/src/main/java") + (filePath.contains("entity") ? "" : "/entity");
                        File entityDirectory = new File(filePath);
                        if (!entityDirectory.exists()) {
                            entityDirectory.mkdir();
                        }
                        File f = new File(filePath + "/" + className + ".java");
                        FileWriter fw;
                        try {
                            fw = new FileWriter(f);
                            fw.write(src);
                            fw.flush();
                            fw.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        DaoUtils.generateDao(className, filePath, keyType, keyProperty);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static File getPojoPackage(Class clazz) throws Exception {
        String filePath;
        if (clazz.getName().contains("entity")) { //是entity包下的类，拷贝继承与实现
            boolean flag = false;
            //是否实现了序列化
            Class[] interfaces = clazz.getInterfaces();
            for (Class inter : interfaces) {
                if (inter.getName().equals("java.io.Serializable")) {
                    flag = true;
                    continue;
                }
            }

            if (flag != true) {
                throw new Exception("Template class has not implement Serializable");
            }

            //是否包含@Entity注解
            Annotation entityAnnotation = clazz.getAnnotation(Entity.class);
            if (entityAnnotation == null) {
                throw new Exception("Template class has no Entity annotation");
            }

            //是否包含@Table注解
            Annotation tableAnnotation = clazz.getAnnotation(Table.class);
            if (tableAnnotation == null) {
                throw new Exception("Template class has no Table annotation");
            }

            filePath = clazz.getResource("").getPath();
            if (!filePath.contains("entity")) {
                filePath = filePath + "/entity";
            }
        } else { //不是entity包下的类，不拷贝继承与实现
            filePath = clazz.getResource("").getPath();
            filePath = filePath + "/entity";
            File pojoPackage = new File(filePath);
            if (!pojoPackage.exists()) {
                throw new Exception("The otherClass must be in the root directory, and entity package must be exists");
            }
        }
        return new File(filePath);
    }

    //约定优于配置
    //约定必须在entity包下至少有一个样板类
    public static void generateTable(MysqlDatabaseConnection connection, Class templateClass) throws Exception {
        File pojoPackage = getPojoPackage(templateClass);
        //自定义classLoader
        GoodOrmClassLoader classLoader = new GoodOrmClassLoader(templateClass);

        if (!pojoPackage.exists()) {
            throw new Exception("entity package does not exists");
        }


        File[] pojoFiles = pojoPackage.listFiles();
        for (File pojoFile : pojoFiles) {
            String pojoPath = pojoFile.getAbsolutePath();
            String pojoName = pojoPath.substring(pojoPath.lastIndexOf("/") + 1, pojoPath.lastIndexOf("."));
            Class pojoClass = classLoader.findClass("entity." + pojoName);

            //检查数据库中是否有该表存在
            Table table = (Table)pojoClass.getAnnotation(Table.class);
            if (table == null) {
                throw new Exception("the entity class " + pojoName + " does not has @Table annotation, please add it");
            } else {
                String tableName = table.name();
                if (StringUtils.isEmpty(tableName)) {
                    throw new Exception("the entity class" + pojoName + " @Table annotation should define table name, plase add it");
                }
            }
            String tableName = table.name();
            String idName;
            Class idClass;
            String fullCrateSql = "";
            String idStartSql = "";
            String idEndSql = "";
            String columnSql = "";
            String uniqueSql = "";
            Connection conn = getConnection(connection);
            DatabaseMetaData dbmd = conn.getMetaData();
            String schema = connection.getSchema();
            ResultSet columnRs = dbmd.getColumns(schema, "%", tableName, "%");
            if (!columnRs.next()) {
                System.out.println("table name is " + tableName + ", not exists");
                fullCrateSql += "CREATE TABLE `" + tableName + "` (" + ln;
                Field[] fields = pojoClass.getDeclaredFields();
                boolean idFlag = false;
                for (Field field : fields) {
                    if (field.getName().equals("serialVersionUID")) {
                        continue;
                    }
                    if (field.getAnnotation(Id.class) != null) {
                        idFlag = true;
                        //如果是id属性
                        idName = convertPropertyNameToColumnName(field.getName());
                        idClass = field.getType();
                        MySqlColumn idColumn = field.getAnnotation(MySqlColumn.class);
                        boolean notNullFlag;
                        boolean unsignedFlag;
                        boolean zeroFillFlag;
                        boolean autoIncreatFlag;
                        String length = "";
                        String defaultValue;
                        PropertyValue propertyValue = new PropertyValue(idClass);
                        ColumnValue columnValue = convertMySqlType(propertyValue);
                        String idType = columnValue.getColumnType();
                        if (idColumn != null) {
                            notNullFlag = idColumn.notNull();
                            unsignedFlag = idColumn.unsigned();
                            zeroFillFlag = idColumn.zeroFill();
                            autoIncreatFlag = idColumn.autoIncrement();
                            length = idColumn.length();
                            defaultValue = idColumn.defaultValue();
                            idStartSql = "\t" + idStartSql + "`" + idName + "`" + " " + idType + " " + (!StringUtils.isEmpty(length) ? "(" + length + ") " : getDefaultLength(idClass))
                                    + (unsignedFlag && canBeUnsigned(idType) ? "UNSIGNED " : "") + (zeroFillFlag && canBeZeroFill(idType) ? "ZEROFILL " : "") + (notNullFlag ? "NOT NULL " : "")
                                    + (autoIncreatFlag && canBeAutoIncrement(idType) ? "AUTO_INCREMENT " : "") + (!StringUtils.isEmpty(defaultValue) ? "DEFAULT " + defaultValue : "") + "," + ln;
                            uniqueSql = uniqueSql + "\t" + "UNIQUE KEY " + "`" + idName + "_UNIQUE" + "`" + " (`" + idName + "`)," + ln;
                            idEndSql = idEndSql + "\t" + "PRIMARY KEY (`" + idName + "`)," + ln;
                        } else {
                            idStartSql = "\t" + idStartSql + "`" + idName + "`" + " " + idType + " "  + (!StringUtils.isEmpty(length) ? "(" + length + ")" : getDefaultLength(idClass)) + " AUTO_INCREMENT" + "," + ln;
                            uniqueSql = uniqueSql + "\t" + "UNIQUE KEY " + "`" + idName + "_UNIQUE" + "`" + " (`" + idName + "`)," + ln;
                            idEndSql = idEndSql + "\t" + "PRIMARY KEY (`" + idName + "`)," + ln;

                        }
                    } else {
                        //如果不是id属性
                        String fieldName = convertPropertyNameToColumnName(field.getName());
                        Class fieldClass = field.getType();
                        MySqlColumn propertyColumn = field.getAnnotation(MySqlColumn.class);
                        PropertyValue propertyValue = new PropertyValue(fieldClass);
                        columnSql = columnSql + convertColumnSql(fieldName, propertyColumn, propertyValue);
                        if (propertyColumn != null && propertyColumn.isUnique()) {
                            uniqueSql = uniqueSql + "\t" + "UNIQUE KEY " + "`" + fieldName + "_UNIQUE" + "`" + " (`" + fieldName + "`)," + ln;
                        }
                    }
                }
                if (!idFlag) {
                    throw new Exception("the entity class " + pojoName +  " does not define @Id annotation on id column, please add it");
                }
                uniqueSql = uniqueSql.substring(0, uniqueSql.length() - 3 ) + ln;
                fullCrateSql = fullCrateSql + idStartSql + columnSql +idEndSql + uniqueSql;
                fullCrateSql = fullCrateSql + ")" + " ENGINE=InnoDB DEFAULT CHARSET=latin1" + ln;
                Statement stsm = conn.createStatement();
                System.out.println("full create sql is " + ln + fullCrateSql);
                stsm.executeUpdate(fullCrateSql);
                conn.close();

                // 生成测试类
                String testPackPath = "com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection.test";
                TestUtils.generateTest(pojoClass, testPackPath);
            }
        }
    }

    private static Connection getConnection(MysqlDatabaseConnection conn) throws Exception {
        StringBuffer connUrlSb = new StringBuffer();
        connUrlSb.append("jdbc:mysql://" + conn.getIp() + ":" + conn.getPort() + "/" + conn.getSchema());
        connUrlSb.append("?characterEncoding=" + conn.getCharacterEncoding());
        connUrlSb.append("&rewriteBatchedStatements=" + conn.getRewriteBatchedStatements());
        connUrlSb.append("&user=" + conn.getUser());
        connUrlSb.append("&password=" + conn.getPassword());
        try {
            return DriverManager.getConnection(connUrlSb.toString());
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 自动生成Java文件内容
     *
     * @param packageName
     * @param className
     * @return
     */
    private static String generateSrc(String packageName, String className, String tableName, String keyProperty, Class templateClass, Map<String, String> propertyMap,  boolean copy) {
        StringBuffer sb = new StringBuffer();
        sb.append("package " + packageName + ";" + ln + ln);
        String superClassName = "";
        String superClassImport = "";
        String tableAnnotationName = "";
        String implementationsName = "";
        String implementationsImport = "";
        String annotationImport = "";
        String propertyImport = "";
        String properties = "";
        //约定优于配置，使用此框架必须安装lombok插件，简化代码
        String lombokImport = "import lombok.Data;" + ln;
        annotationImport = annotationImport + "import javax.persistence.Entity;" + ln;
        annotationImport = annotationImport + "import javax.persistence.Id;" + ln;
        annotationImport = annotationImport + "import javax.persistence.Table;" + ln;
        tableAnnotationName = tableAnnotationName + "@Data" + ln;
        tableAnnotationName = tableAnnotationName + "@Entity" + ln;
        tableAnnotationName = tableAnnotationName + "@Table(name = \"" + tableName + "\")" + ln;
        //在拷贝标志的同时，要确定被拷贝类来自entity类，否则不拷贝
        if (copy && templateClass.getName().contains("entity")) {
            Class superClass = templateClass.getSuperclass();
            if (superClass != null && !superClass.getName().equals("java.lang.Object")) {
                superClassName = superClass.getSimpleName();
                System.out.println("super class path is " + superClass.getResource("").getPath());
                //判断父类是否在本包内
                if (!isSupperClassInTheSamePackage(templateClass, superClass)) {
                    superClassImport += "import " + superClass.getName() + ";" + ln;
                }
            }
            Class[] implementations = templateClass.getInterfaces();
            if (!ArrayUtils.isEmpty(implementations)) {
                for (Class implementation : implementations) {
                    implementationsName += implementation.getSimpleName() + ", ";
                    //判断接口是否在本包内
                    if (!isImplementationInTheSamePackage(templateClass, implementation)) {
                        implementationsImport += "import " + implementation.getName() + ";" + ln;
                    }
                }
                implementationsName = implementationsName.substring(0, implementationsName.length() - 2);
            }
        } else {
            implementationsName = "Serializable";
            implementationsImport = "import java.io.Serializable;" + ln;
        }
        Set<String> set = new HashSet<String>();
        if (!propertyMap.isEmpty()) {
            Iterator<Map.Entry<String, String>> iter = propertyMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String typeClassName = entry.getValue();
                String propertyName = entry.getKey();
                if (typeClassName !=null && !typeClassName.contains("java.lang")) { //基础类型不需要import
                    set.add("import " + typeClassName + ";" + ln);
                }
                String typeSimpleName = typeClassName.substring(typeClassName.lastIndexOf(".") + 1);
                if (propertyName.equals(keyProperty)) {
                    properties += "\t" + "@Id" + ln;
                }
                properties += "\t" + "private " + typeSimpleName + " " + propertyName + ";" + ln + ln;
            }

        }
        if (!set.isEmpty()) {
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()) {
                propertyImport += iter.next();
            }
        }

        sb.append(implementationsImport);
        sb.append(lombokImport);
        sb.append(annotationImport);
        sb.append(superClassImport);
        sb.append(propertyImport);
        sb.append(ln);
        sb.append(tableAnnotationName);
        sb.append("public class " + className + (StringUtils.isEmpty(superClassName) ? "" : " extends " + superClassName + " ") + (StringUtils.isEmpty(implementationsName) ? "" : " implements " + implementationsName + " ") + "{" + ln + ln);
        sb.append(properties);
//        sb.append(setterGetter);
        sb.append("}" + ln);
        return sb.toString();
    }

    /**
     * 将表名转换为类名，表名规定字符以下划线分割，类名采用驼峰式
     *
     * @param tableName
     * @return
     */
    private static String convertTableNameToClassName(String tableName) {
        StringBuffer classSb = new StringBuffer();
        String[] tableWords = tableName.split("_");
        if (!ArrayUtils.isEmpty(tableWords)) {
            for (String str : tableWords) {
                classSb.append(convertFst(str));
            }
            return classSb.toString();
        } else {
            return convertFst(tableName);
        }
    }

    private static String convertFst(String word) {
        StringBuffer wordSb = new StringBuffer();
        return wordSb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1, word.length())).toString();
    }

    //判断样板类与父类是否在同一个包内
    private static boolean isSupperClassInTheSamePackage(Class templateClass, Class superClass) {
        return templateClass.getPackage().equals(superClass.getPackage());
    }

    //判断样板类与接口是否在统一包内
    private static boolean isImplementationInTheSamePackage(Class templateClass, Class implementationClass) {
        return templateClass.getPackage().equals(implementationClass.getPackage());
    }

    //id是否可以步增
    private static boolean isIncresable(String columnType) {
        String[] incresableColumnTypes = {"SMALLINT", "INTEGER", "BIGINT", "FLOAT", "DOUBLE", "DECIMAL"};
        return Arrays.asList(incresableColumnTypes).contains(columnType);
    }

    //是否需要提供默认长度
    private static boolean needProvideLength(String columnType) {
        String[] provideLengthColumnTypes = {"INT", "SMALLINT", "BIGINT", "DECIMAL", "TIME", "YEAR", "TIMESTAMP", "CHAR", "VARCHAR", "BIT"};
        return Arrays.asList(provideLengthColumnTypes).contains(columnType);
    }

    //是否可以指定unsigned
    private static boolean canBeUnsigned(String columnType) {
        String[] canBeUnsignedColumnTypes = {"INT", "SMALLINT", "BIGINT"};
        return Arrays.asList(canBeUnsignedColumnTypes).contains(columnType);
    }

    //是否可以指定zerofill
    private static boolean canBeZeroFill(String columnType) {
        String[] canBeZeroFillColumnTypes = {"INT", "SMALLINT", "BIGINT"};
        return Arrays.asList(canBeZeroFillColumnTypes).contains(columnType);
    }

    //是否可以指定autoIncrement
    private static boolean canBeAutoIncrement(String columnType) {
        String[] canBeAutoIncrementTypes = {"INT", "SMALLINT", "BIGINT"};
        return Arrays.asList(canBeAutoIncrementTypes).contains(columnType);
    }

    private static String getDefaultLength(Class propertyClass) {
        if (propertyClass.getName().equals("java.lang.String")) {
            return "(255)";
        } else if (propertyClass.getName().equals("java.lang.Long")) {
            return "(20)";
        } else if (propertyClass.getName().equals("java.lang.Integer")) {
            return "(11)";
        }
        return "";
    }

    private static String convertColumnSql(String fieldName, MySqlColumn sqlColumn, PropertyValue propertyValue) {
        boolean notNullFlag = false;
        boolean unsignedFlag = false;
        boolean zeroFillFlag = false;
        boolean autoIncreatFlag = false;
        String length = "";
        String defaultValue = "";
        Class propertyClass = propertyValue.getPropertyClass();
        ColumnValue columnValue = convertMySqlType(propertyValue);
        String columnType = columnValue.getColumnType();
        if (sqlColumn != null) {
            notNullFlag = sqlColumn.notNull();
            unsignedFlag = sqlColumn.unsigned();
            zeroFillFlag = sqlColumn.zeroFill();
            autoIncreatFlag = sqlColumn.autoIncrement();
            length = sqlColumn.length();
            defaultValue = sqlColumn.defaultValue();
        }
        return "\t" + "`" + fieldName + "`" + " " + columnType + (!StringUtils.isEmpty(length) ? "(" + length + ") " : getDefaultLength(propertyClass))
                + (unsignedFlag && canBeUnsigned(columnType) ? "UNSIGNED " : "") + (zeroFillFlag && canBeZeroFill(columnType) ? "ZEROFILL " : "") + (notNullFlag ? "NOT NULL " : "")
                + (autoIncreatFlag && canBeAutoIncrement(columnType) ? "AUTO_INCREMENT " : "") + (!StringUtils.isEmpty(defaultValue) ? "DEFAULT " + defaultValue : "") + "," + ln;
    }

    /**
     * 将属性名转换为表中的列名
     * 约定优于配置
     * 约定如果属性名中包含下弧线，则列名等于属性名
     * 约定如果属性名中不包含下划线，则将属性名按大写字母分割后全部转为小写并用下划线拼接作为列名
     *
     * @param propertyName
     * @return
     */
    public static String convertPropertyNameToColumnName(String propertyName) {
        if (propertyName.contains("_")) {
            return propertyName;
        } else {
            String columnName = "";
            for (char c : propertyName.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    columnName += "_" + Character.toLowerCase(c);
                } else {
                    columnName += c;
                }
            }
            return columnName;
        }
    }

    //规则优于配置
    //数据库表列名首字母小写，以下划线隔开
    public static String convertColumnNameToPropertyName(String columnName) {
        StringBuffer propertySb = new StringBuffer();
        boolean fstFlag = false;
        String[] columnWords = columnName.split("_");
        for (String word : columnWords) {
            if (fstFlag) {
                word = word.substring(0, 1).toUpperCase() + word.substring(1);
            }
            fstFlag = true;
            propertySb.append(word);

        }
        return propertySb.toString();
    }

    public static Map<String, String> convertColumnMapToPropertyMap(Map<String, String> map) {
        Map<String, String> propertyMap = new HashMap<String, String>();
        Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String columnName = entry.getKey();
            columnName = convertColumnNameToPropertyName(columnName);
            String columnType = entry.getValue();
            columnType = convertJavaType(columnType);
            propertyMap.put(columnName, columnType);
        }
        return propertyMap;
    }


    public static List<String> convertFieldList(List<Field> fields) {
        List<String> list = new ArrayList<String>();
        for (Field field : fields) {
            list.add(field.toString());
        }
        return list;
    }

    public static String convertJavaType(String columnType) {
        if (columnType.contains("TINYINT") || columnType.contains("SMALLINT") || columnType.contains("MEDIUMINT") || columnType.contains("INT")) {
            return "java.lang.Integer";
        } else if (columnType.contains("INTEGER")  || columnType.contains("BIGINT")) {
            return "java.lang.Long";
        } else if (columnType.contains("FLOAT")) {
            return "java.lang.Float";
        } else if (columnType.contains("DOUBLE")) {
            return "java.lang.Double";
        } else if (columnType.contains("DECIMAL")) {
            return "java.math.BigDecimal";
        } else if (columnType.contains("DATA") || columnType.contains("YEAR")) {
            return "java.util.Date";
        } else if (columnType.contains("TIME")) {
            return "java.sql.Time";
        } else if (columnType.contains("DATETIME") || columnType.contains("TIMESTAMP")) {
            return "java.sql.Timestamp";
        } else if (columnType.contains("CHAR") || columnType.contains("VARCHAR")) {
            return "java.lang.String";
        } else if (columnType.contains("TINYBLOB") || columnType.contains("TINYTEXT") || columnType.contains("BLOB") || columnType.contains("TEXT") || columnType.contains("MEDIUMBLOB") || columnType.contains("MEDIUMTEXT") || columnType.contains("LONGBLOB") || columnType.contains("LONGTEXT")) {
            return ("com.mysql.cj.jdbc.Blob");
        } else if (columnType.contains("BIT")) {
            return ("java.lang.Boolean");
        } else {
            return null;
        }
    }

    public static ColumnValue convertMySqlType(PropertyValue propertyValue) {
        Class propertyClass = propertyValue.getPropertyClass();
        ColumnValue columnValue = new ColumnValue();
        if (propertyClass.getName().equals("java.lang.Integer")) {
            columnValue.setColumnType("INT");
        } else if (propertyClass.getName().equals("java.lang.Long")) {
            columnValue.setColumnType("BIGINT");
        } else if (propertyClass.getName().equals("java.lang.Float")) {
            columnValue.setColumnType("FLOAT");
        } else if (propertyClass.getName().equals("java.lang.Double")) {
            columnValue.setColumnType("DOUBLE");
        } else if (propertyClass.getName().equals("java.math.BigDecimal")) {
            columnValue.setColumnType("DECIMAL");
        } else if (propertyClass.getName().equals("java.util.Date")) {
            columnValue.setColumnType("DATE");
        } else if (propertyClass.getName().equals("java.sql.Time")) {
            columnValue.setColumnType("TIME");
        } else if (propertyClass.getName().equals("java.sql.Timestamp")) {
            columnValue.setColumnType("DATETIME");
        } else if (propertyClass.getName().equals("java.lang.String")) {
            columnValue.setColumnType("VARCHAR");
        } else if (propertyClass.getName().equals("com.mysql.cj.jdbc.Blob")) {
            columnValue.setColumnType("TEXT");
        } else if (propertyClass.getName().equals("java.lang.Boolean")) {
            columnValue.setColumnType("BIT");
        } else {
            return null;
        }
        return columnValue;
    }
}
