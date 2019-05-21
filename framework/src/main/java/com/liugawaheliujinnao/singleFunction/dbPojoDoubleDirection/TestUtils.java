package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

import javax.persistence.Id;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @Description: 测试工具类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public class TestUtils {

    public static final String ln = "\r\n";

    public static void generateTest(Class pojoClass, String testPackPath) {
        String pojoSimpleName = pojoClass.getSimpleName();
        String pojoPropertyName = pojoSimpleName.substring(0, 1).toLowerCase() + pojoSimpleName.substring(1);
        String insertPropertySet = "";
        String insertUnIdPropertySet = "";
        String updatePropertySet = "";
        Field[] fields = pojoClass.getDeclaredFields();
        String fieldName = "";
        Class fieldType;
        String fieldTypeName;
        String idGet = "";
        String idSet = "";
        String fstUnIdSet = "";
        Object insertVal = null;
        Object updateVal = null;
        Object id = null;
        Object fstUnId = null;
        String idSuffix = "";
        String fstUnIdSuffix = "";
        boolean dateFlag = false;
        boolean fstUnIdColumn = false;
        for (Field field : fields) {
            if (field.getAnnotation(Id.class) != null) {
                fieldName = field.getName();
                idGet = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                idSet = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                fieldTypeName = field.getType().getName();
                if (fieldTypeName.equals("java.lang.Integer")) {
                    id = 1;
                    idSuffix = "";
                } else if (fieldTypeName.equals("java.lang.Long")) {
                    id = 1L;
                    idSuffix = "L";
                } else if (fieldTypeName.equals("java.lang.Float")) {
                    id = 1F;
                    idSuffix = "F";
                } else if (fieldTypeName.equals("java.lang.Double")) {
                    id = 1D;
                    idSuffix = "D";
                }
            } else {
                fieldName = field.getName();
                if (fieldName != "serialVersionUID" &&!fstUnIdColumn) {
                    fstUnIdSet = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    fieldTypeName = field.getType().getName();
                    if (fieldTypeName.equals("java.lang.Integer")) {
                        fstUnId = 1;
                        fstUnIdSuffix = "";
                    } else if (fieldTypeName.equals("java.lang.Long")) {
                        fstUnId = 1L;
                        fstUnIdSuffix = "L";
                    } else if (fieldTypeName.equals("java.lang.Float")) {
                        fstUnId = 1F;
                        fstUnIdSuffix = "F";
                    } else if (fieldTypeName.equals("java.lang.Double")) {
                        fstUnId = 1D;
                        fstUnIdSuffix = "D";
                    } else if (fieldTypeName.equals("java.lang.String")) {
                        fstUnId = "\"1\"";
                        fstUnIdSuffix = "";
                    } else if (fieldTypeName.equals("java.util.Date")) {
                        fstUnId = "new Date()";
                        fstUnIdSuffix = "";
                        if (!dateFlag) {
                            dateFlag = true;
                        }
                    }
                    insertUnIdPropertySet = insertUnIdPropertySet + "\t\t" + pojoPropertyName + "." + fstUnIdSet + "(" + fstUnId + fstUnIdSuffix + ");" + ln;
                }
            }

            if (field.getAnnotation(UnNullable.class) != null) {
                fieldName = field.getName();
                String fieldSet = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                fieldType = field.getType();
                fieldTypeName = fieldType.getName();
                if (fieldTypeName.equals("java.lang.Integer")) {
                    insertVal = 1;
                    updateVal = 2;
                } else if (fieldTypeName.equals("java.lang.Long")) {
                    insertVal = 1L;
                    updateVal = 2L;
                } else if (fieldTypeName.equals("java.lang.Float")) {
                    insertVal = 1F;
                    updateVal = 2F;
                } else if (fieldTypeName.equals("java.lang.Double")) {
                    insertVal = 1D;
                    updateVal = 2D;
                } else if (fieldTypeName.equals("java.lang.Boolean")) {
                    insertVal = true;
                    updateVal = false;
                } else if (fieldTypeName.equals("java.lang.String")) {
                    insertVal = "\"insertTest\"";
                    updateVal = "\"updateTest\"";
                }
                insertPropertySet = insertPropertySet + "\t\t" + pojoPropertyName + "." + fieldSet + "(" + insertVal + ")";
                updatePropertySet = updatePropertySet + "\t\t" + pojoPropertyName + "." + fieldSet + "(" + updateVal + ")";
            }
        }

        StringBuffer testSrc = new StringBuffer();

        String pojoTestSimpleName = pojoSimpleName + "DaoTest";
        String pojoName = pojoClass.getName();
        String pojoPack = pojoClass.getPackage().getName();
        String testPack = "import " + pojoPack.substring(0, pojoPack.lastIndexOf("."));
        testPack = testPack.substring(0, testPack.lastIndexOf(".")) + ".test;";
        String junitImport = "import org.junit.Test;" + ln + "import org.junit.runner.RunWith;" + ln;
        String pojoClassImport = "import " + pojoClass.getName() + ";" + ln;
        String pojoImport = pojoName + "Dao" + ";" + ln;
        String daoImport = "import " + pojoImport.replace("entity", "dao");
        String springImport = "import org.springframework.beans.factory.annotation.Autowired;" + ln;
        springImport = springImport + "import org.springframework.test.context.ContextConfiguration;" + ln;
        springImport = springImport + "import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;" + ln;
        String listImport = "import java.util.List;" + ln;
        String dateImport = "import java.util.Date;" + ln;
        String testAnnotation = "@ContextConfiguration(locations = {\"classpath*:application-context.xml\"})" + ln;
        testAnnotation = testAnnotation + "@RunWith(SpringJUnit4ClassRunner.class)" + ln;
        testSrc.append("package " + testPackPath + ";" + ln + ln);
        testSrc.append(junitImport);
        testSrc.append(daoImport);
        testSrc.append(pojoClassImport);
        testSrc.append(springImport + ln);
        testSrc.append(dateFlag ? dateImport : "");
        testSrc.append(listImport);
        testSrc.append(ln);

        testSrc.append(testAnnotation);
        testSrc.append("public class " + pojoSimpleName + "DaoTest {" + ln);
        testSrc.append(ln);
        testSrc.append("\t@Autowired" + ln);
        testSrc.append("\tprivate " + pojoSimpleName + "Dao" + " " + pojoPropertyName + "Dao;" + ln);
        testSrc.append(ln);

        // insertOne method
        testSrc.append("\t@Test" + ln);
        testSrc.append("\tpublic void insertOne() throws Exception {" + ln);
        testSrc.append("\t\t" + pojoSimpleName + " " + pojoPropertyName + " = " + "new " + pojoSimpleName + "();" + ln);
        testSrc.append(insertPropertySet);
        testSrc.append(insertUnIdPropertySet);
        testSrc.append("\t\tassert (" + pojoPropertyName + "Dao" + "." + "insertOne(" + pojoPropertyName + "));" + ln);
        testSrc.append("\t}" + ln);
        testSrc.append(ln);

        // updateOne method
        testSrc.append("\t@Test" + ln);
        testSrc.append("\tpublic void updateOne() throws Exception {" + ln);
        testSrc.append("\t\t" + pojoSimpleName + " " + pojoPropertyName + " = " + "new " + pojoSimpleName + "();" + ln);
        testSrc.append(updatePropertySet);
        testSrc.append("\t\tassert (" + pojoPropertyName + "Dao" + "." + "updateOne(" + pojoPropertyName + "));" + ln);
        testSrc.append("\t}" + ln);
        testSrc.append(ln);

        // selectOne method
        testSrc.append("\t@Test" + ln);
        testSrc.append("\tpublic void selectOne() throws Exception {" + ln);
        testSrc.append("\t\t" + pojoSimpleName + " " + pojoPropertyName + " = " + pojoPropertyName + "Dao" + "." + "selectOne(" + id + idSuffix + ");" + ln);
        testSrc.append("\t\tassert (" + pojoPropertyName + "." + idGet + "()" + ".equals(" + id + "));" + ln);
        testSrc.append("\t}" + ln);
        testSrc.append(ln);

        // selectAll method
        testSrc.append("\t@Test" + ln);
        testSrc.append("\tpublic void selectAll() throws Exception {" + ln);
        testSrc.append("\t\tList<" + pojoSimpleName + "> list = " + pojoPropertyName + "Dao" + "." + "selectAll();" + ln);
        testSrc.append("\t\tassert (list.size() == 1);" + ln);
        testSrc.append("\t}" + ln);
        testSrc.append(ln);

        // deleteOne method
        testSrc.append("\t@Test" + ln);
        testSrc.append("\tpublic void deleteOne() throws Exception {" + ln);
        testSrc.append("\t\t" + pojoSimpleName + " " + pojoPropertyName + " = " + "new " + pojoSimpleName + "();" + ln);
        testSrc.append("\t\t" + pojoPropertyName + "." + idSet + "(" + id + idSuffix +  ");" + ln);
        testSrc.append("\t\tassert (" + pojoPropertyName + "Dao" + "." + "deleteOne(" + pojoPropertyName + "));" + ln);
        testSrc.append("\t}" + ln);

        // end
        testSrc.append("}");

        String filePath = new File(pojoClass.getResource("").getPath()).getPath();
        String testFilePath = filePath.substring(0, filePath.indexOf("/target"));
        testFilePath = testFilePath + "/src/test/java/" + testPackPath.replaceAll("\\.", "/");
        System.out.println(testFilePath);
        File testFile = new File(testFilePath);
        if (!testFile.exists()) {
            testFile.mkdir();
        }
        File f = new File(testFilePath + "/" + pojoTestSimpleName + ".java");
        FileWriter fw;
        try {
            fw = new FileWriter(f);
            fw.write(testSrc.toString());
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
