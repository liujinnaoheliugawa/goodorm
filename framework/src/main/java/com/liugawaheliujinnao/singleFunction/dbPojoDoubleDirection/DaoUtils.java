package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @Description: Dao 工具类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public class DaoUtils {

    public static final String ln = "\r\n";

    public static void generateDao(String pojoSimpleName, String pojoFilePath, String pojoKeyType, String pojoKeyProperty) {
        String pojoName = pojoFilePath.substring(pojoFilePath.indexOf("java/") + 5).replace("/src/main/java", "").replaceAll("/", ".");
//        String pojoSimpleName = pojoName.substring(pojoName.lastIndexOf(".") + 1);
        String pojoPack = pojoName.substring(0, pojoName.lastIndexOf("."));
        String daoPack= pojoPack + ".dao";
        String daoSimpleName = pojoSimpleName.substring(0, 1).toUpperCase() + pojoSimpleName.substring(1) + "Dao";
        String daoName = daoPack + "." + daoSimpleName;
        String daoImplPack = "";
        String daoImplName = daoSimpleName + "Impl";
        String pojoImport = "";
        String daoImport = "";
        String goodOrmImport = "";
        String listImport = "";
        String repositoryImport = "";
        String resourceImport = "";
        String dataSourceImport = "";
        String idImport = "";
        String idSimpleName = "";
        String idName = "";
        try {
            Class.forName(daoName);
        } catch (ClassNotFoundException e) {
            // dao类不存在,新建dao
            daoImplPack = daoPack + ".impl";
            daoImport = "import " + daoName + ";" + ln;
            pojoImport = "import " + pojoName +  "." + pojoSimpleName + ";" + ln;
            goodOrmImport = "import " + BaseDaoSupport.class.getName() + ";" + ln +
                    "import " + QueryRule.class.getName() + ";" + ln;
            repositoryImport = "import " + Repository.class.getName() + ";" + ln;
            resourceImport = "import " + Resource.class.getName() + ";" + ln;
            dataSourceImport = "import " + DataSource.class.getName() + ";" + ln;
            listImport = "import " + List.class.getName() + ";" + ln + ln;

            if (pojoKeyType != null && !pojoKeyType.contains("java.lang")) {
                idImport = "import " + pojoKeyType + ";" + ln;
            }
            idSimpleName = pojoKeyType.substring(pojoKeyType.lastIndexOf(".") + 1);
            idName = pojoKeyProperty;


            String fileDaoPath = pojoFilePath.replace("/entity", "/dao");
            String fileDaoImplPath = fileDaoPath + "/impl";
            File daoDirectory = new File(fileDaoPath);
            if (!daoDirectory.exists()) {
                daoDirectory.mkdir();
            }
            File daoImplDirectory = new File(fileDaoImplPath);
            if (!daoImplDirectory.exists()) {
                daoImplDirectory.mkdir();
            }

            //组装dao
            String daoSrc = generateDaoFile(daoPack, listImport, daoSimpleName, pojoImport, idImport, pojoSimpleName, idSimpleName);

            File daoFile = new File(fileDaoPath + "/" + daoSimpleName + ".java");
            FileWriter fw;
            try {
                fw = new FileWriter(daoFile);
                fw.write(daoSrc);
                fw.flush();
                fw.close();

                //组装daoImpl
                String daoImplSrc = generateDaoImplFile(daoImplPack, daoImport, pojoImport,
                        goodOrmImport, repositoryImport, resourceImport, dataSourceImport,
                        listImport, idImport, daoImplName, pojoSimpleName, idSimpleName,
                        daoSimpleName, idName);

                File daoImplFile = new File(fileDaoPath + "/impl/" + daoSimpleName + "Impl" + ".java");
                fw = new FileWriter(daoImplFile);
                fw.write(daoImplSrc);
                fw.flush();
                fw.close();
            } catch (IOException ex) {

            }
        }
    }

    private static String generateDaoFile(String daoPack, String listImport, String daoSimpleName, String pojoImport, String idImport, String pojoSimpleName, String idSimpleName) {
        StringBuffer daoSb = new StringBuffer();
        //组装dao
        daoSb.append("package " + daoPack + ";" + ln + ln);
        daoSb.append(listImport);
        daoSb.append(pojoImport);
        daoSb.append(idImport);
        daoSb.append(ln);
        daoSb.append("public interface " + daoSimpleName + " {" + ln + ln);
        daoSb.append("\t" + pojoSimpleName + " selectOne(" + idSimpleName + " id) throws Exception;" + ln + ln);
        daoSb.append("\t" + "List<" + pojoSimpleName + "> selectAll() throws Exception;" + ln + ln);
        daoSb.append("\t" + "boolean insertOne(" + pojoSimpleName + " m) throws Exception;" + ln + ln);
        daoSb.append("\t" + "boolean updateOne(" + pojoSimpleName + " m) throws Exception;" + ln + ln);
        daoSb.append("\t" + "boolean deleteOne(" + pojoSimpleName + " m) throws Exception;" + ln);
        daoSb.append("}");
        return daoSb.toString();
    }

    private static String generateDaoImplFile(String daoImplPack, String daoImport, String pojoImport,
                                              String goodOrmImport, String repositoryImport, String resourceImport, String dataSourceImport,
                                              String listImport, String idImport, String daoImplName, String pojoSimpleName, String idSimpleName,
                                              String daoSimpleName, String idName) {
        StringBuffer daoImplSb = new StringBuffer();

        //组装daoImpl
        daoImplSb.append("package " + daoImplPack + ";" + ln + ln);
        daoImplSb.append(daoImport);
        daoImplSb.append(pojoImport);
        daoImplSb.append(goodOrmImport);
        daoImplSb.append(repositoryImport);
        daoImplSb.append(resourceImport);
        daoImplSb.append(dataSourceImport);
        daoImplSb.append(listImport);
        daoImplSb.append(idImport);
        daoImplSb.append("@Repository" + ln);
        daoImplSb.append("public class " + daoImplName + " extends BaseDaoSupport<" + pojoSimpleName + ", " + idSimpleName + "> implements " + daoSimpleName + " {" + ln + ln);

        // Override getPKColumn method
        daoImplSb.append("\t" + "@Override" + ln);
        daoImplSb.append("\t" + "protected String getPKColumn() {" + ln);
        daoImplSb.append("\t\t" + "return " + "\"" + idName + "\";" + ln);
        daoImplSb.append("\t}" + ln + ln);

        // Override setDataSource method
        daoImplSb.append("\t" + "@Resource(name=\"dataSource\")" + ln);
        daoImplSb.append("\t" + "protected void setDataSource(DataSource dataSource) {" + ln);
        daoImplSb.append("\t\t" + "super.setDataSourceReadOnly(dataSource);" + ln);
        daoImplSb.append("\t\t" + "super.setDataSourceWrite(dataSource);" + ln);
        daoImplSb.append("\t}" + ln + ln);

        // Override selectOne method
        daoImplSb.append("\t" + "public " + pojoSimpleName + " selectOne(" + idSimpleName + " id) throws Exception {" + ln);
        daoImplSb.append("\t\t" + "QueryRule queryRule =" + ln);
        daoImplSb.append("\t\t\t" + "QueryRule.getInstance()" + ln);
        daoImplSb.append("\t\t\t\t" + ".andEqual(\"" + idName + "\", id)" + ln);
        daoImplSb.append("\t\t\t\t" + ".addAscOrder(\"" + idName + "\");" + ln);
        daoImplSb.append("\t\t" + "return super.findUnique(queryRule);" + ln);
        daoImplSb.append("\t}" + ln + ln);

        // Override selectAll method
        daoImplSb.append("\t" + "public " + "List<" + pojoSimpleName + "> selectAll() throws Exception {" + ln);
        daoImplSb.append("\t\t" + "return super.getAll();" + ln);
        daoImplSb.append("\t" + "}" + ln + ln);

        // Override insertOne method
        daoImplSb.append("\t" + "public boolean insertOne(" + pojoSimpleName + " m) throws Exception {" + ln);
        daoImplSb.append("\t\t" + idSimpleName + " id = super.insertAndReturnId(m);" + ln);
        daoImplSb.append("\t\t" + "m.set" + idName.substring(0, 1).toUpperCase() + idName.substring(1) + "(id);" + ln);
        daoImplSb.append("\t\t" + "return id > 0;" + ln);
        daoImplSb.append("\t" + "}" + ln + ln);

        // Override updateOne method
        daoImplSb.append("\t" + "public boolean updateOne(" + pojoSimpleName + " m) throws Exception {" + ln);
        daoImplSb.append("\t\t" + "long count = super.update(m);" + ln);
        daoImplSb.append("\t\t" + "return count > 0;" + ln);
        daoImplSb.append("\t" + "}" + ln + ln);

        // Override deleteOne method
        daoImplSb.append("\t" + "public boolean deleteOne(" + pojoSimpleName + " m) throws Exception {" + ln);
        daoImplSb.append("\t\t" + "long count = super.delete(m);" + ln);
        daoImplSb.append("\t\t" + "return count > 0;" + ln);
        daoImplSb.append("\t" + "}" + ln + ln);

        // end tag
        daoImplSb.append("}");

        return daoImplSb.toString();
    }
}
