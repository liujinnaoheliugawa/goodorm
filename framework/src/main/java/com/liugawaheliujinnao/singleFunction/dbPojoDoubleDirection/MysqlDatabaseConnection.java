package com.liugawaheliujinnao.singleFunction.dbPojoDoubleDirection;

/**
 * @Description: Mysql 数据库连接类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-21
 */
public class MysqlDatabaseConnection {

    private String ip;

    private Integer port;

    private String schema;

    private String characterEncoding;

    private String rewriteBatchedStatements;

    private String user;

    private String password;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getRewriteBatchedStatements() {
        return rewriteBatchedStatements;
    }

    public void setRewriteBatchedStatements(String rewriteBatchedStatements) {
        this.rewriteBatchedStatements = rewriteBatchedStatements;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MysqlDatabaseConnection() {

    }

    public MysqlDatabaseConnection(String ip, Integer port, String schema, String characterEncoding, String rewriteBatchedStatements, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.schema = schema;
        this.characterEncoding = characterEncoding;
        this.rewriteBatchedStatements = rewriteBatchedStatements;
        this.user = user;
        this.password = password;
    }

    public MysqlDatabaseConnection(String schema) {
        this(3306, schema);
    }

    public MysqlDatabaseConnection(Integer port, String schema) {
        this("127.0.0.1", port, schema);
    }

    public MysqlDatabaseConnection(String ip, String schema) {
        this(ip, 3306, schema);
    }

    public MysqlDatabaseConnection(String ip, Integer port, String schema) {
        this(ip, port, schema, "UTF-8");
    }

    public MysqlDatabaseConnection(String user, String password, String schema) {
        this("127.0.0.1", 3306, schema, "UTF-8", "true", user, password);
    }

    public MysqlDatabaseConnection(String ip, Integer port, String schema, String characterEncoding) {
        this(ip, port, schema, characterEncoding, "true");
    }

    public MysqlDatabaseConnection(String ip, Integer port, String schema, String characterEncoding, String rewriteBatchedStatements) {
        this(ip, port, schema, characterEncoding, rewriteBatchedStatements, "root", "root");
    }
}
