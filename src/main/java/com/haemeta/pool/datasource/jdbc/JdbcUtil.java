package com.haemeta.pool.datasource.jdbc;

import com.haemeta.common.enums.ResultStatusCodes;
import com.haemeta.common.exception.HaemetaException;
import com.haemeta.common.utils.lang.ListUtil;
import com.haemeta.common.utils.lang.StringUtil;
import com.haemeta.pool.datasource.jdbc.entity.DtoCache;
import com.haemeta.pool.datasource.jdbc.entity.DtoInfo;
import com.haemeta.pool.datasource.jdbc.entity.HashMapDto;
import com.haemeta.pool.datasource.jdbc.entity.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class JdbcUtil {

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected DataSource dataSource;
    protected CloseLambda closeCall;

    public JdbcUtil(DataSource dataSource){
        this.dataSource = dataSource;
        closeCall = (conn, ps, rs) -> {

            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (ps != null) {
                    ps.close();
                    ps = null;
                }


                //查看 事务队列中，是否包含事务，不包含事务，就回收连接
                if (conn != null) {
                    conn.close();
                    conn = null;
                }

            } catch (SQLException throwables) {
                log.error(getClass() + ": 【PreparedStatement.close()】 failed");
                try {
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException e) {
                    log.error(getClass() + ": 【PreparedStatement.close()】 failed");
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException sqlException) {
                    log.error(getClass() + ": 【line:207 private void close()】 failed");
                    log.error(getClass() + ": 【Connection.close()】 failed");
                }
                }

            }
        };
    }
    public JdbcUtil(DataSource dataSource,CloseLambda closeCall){
        this.dataSource = dataSource;
        this.closeCall = closeCall;
    }

    /**
     * 用SQL实体类封装 查询单条SQL
     *
     * @param sql
     * @return
     */
    public SQL select(SQL sql) throws HaemetaException {
        List list = new ArrayList();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            //查 list ---------------------------------------------------------------*
            pstmt = conn.prepareStatement(sql.getSql());
            rs = executeQuery(pstmt, sql.getParameters());
            sql.setResult(getFromResultSet(rs));

        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } finally {
            close(conn, pstmt, rs);
        }

        return sql;
    }

    public <Bean> Boolean update(Bean bean) throws HaemetaException {
        if (bean == null) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer rs = null;

        SQL sql = getUpdateSQL(SQL.of(bean));

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.getSql());

            //注入参数
            setParams(pstmt, sql.getParameters());

            //执行SQL
            log.info(pstmt.toString());
            rs = pstmt.executeUpdate();

        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } finally {
            close(conn, pstmt, null);
        }

        return rs > 0 ? true : false;
    }

    public Boolean update(SQL sql) throws HaemetaException {
        if (sql == null) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer rs = null;

        getUpdateSQL(sql);

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql.getSql());

            //注入参数
            setParams(pstmt, sql.getParameters());


            //执行SQL
            log.info(pstmt.toString());
            rs = pstmt.executeUpdate();

        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } finally {
            close(conn, pstmt, null);
        }

        return rs > 0 ? true : false;
    }

    @Deprecated
    public <Bean> Boolean updateSetNull(Bean bean, String... targets) throws HaemetaException {
        if (bean == null)
            return false;

        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer rs = null;

        DtoInfo info = DtoCache.getDtoInfo(bean);

        Map<String, Object> paramMap = notNullValues(bean,info);

        //SQL: UPDATE XX SET
        StringBuffer sql = new StringBuffer("UPDATE ").append(info.getTableName()).append(" SET ");

        List param = new ArrayList();

        //SQL: UPDATE XX SET AA=?,BB=?
        int keyIndex = 0;
        Set<String> keys = paramMap.keySet();
        for (String key : keys) {
            if (key.equals("id")) {
                continue;
            }
            sql.append(key).append("=?");
            param.add(paramMap.get(key));
            keyIndex++;
            if (keyIndex != keys.size() - 1) {
                sql.append(",");
            }
        }

        for (int i = 0; i < targets.length; i++) {
            sql.append(",").append(targets[i]).append("=Null");
        }

        //SQL: UPDATE XX SET AA=?,BB=? WHERE id=?
        sql.append(" WHERE id=?");
        param.add(paramMap.get("id"));

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql.toString());

            //注入参数
            for (int i = 1; i <= param.size(); i++) {
                pstmt.setObject(i, param.get(i - 1));
            }

            //执行SQL
            log.info(pstmt.toString());
            rs = pstmt.executeUpdate();

        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } finally {
            close(conn, pstmt, null);
        }

        return rs > 0 ? true : false;
    }


    public <Bean> Boolean insert(Bean bean) throws HaemetaException {
        if (bean == null) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rs = 0;

        SQL insertSQL = getInsertSQL(SQL.of(bean));
        String sql = insertSQL.getSql();
        List param = insertSQL.getParameters();

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql);

            //注入参数
            for (int i = 1; i <= param.size(); i++) {
                pstmt.setObject(i, param.get(i - 1));
            }

            //执行SQL
            log.info(pstmt.toString());
            rs = pstmt.executeUpdate();
        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } finally {
            close(conn, pstmt, null);
        }

        return rs > 0 ? true : false;
    }

    public <Bean> Boolean insert(SQL<Bean> sql) throws HaemetaException {

        PreparedStatement pstmt = null;
        int rs = 0;
        Connection conn = null;

        String sqlStr = getInsertSQL(sql).getSql();
        List param = sql.getParameters();

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sqlStr);

            //注入参数
            setParams(pstmt, param);

            //执行SQL
            log.info(pstmt.toString());
            rs = pstmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new HaemetaException(exception);
        } finally {
            close(conn, pstmt, null);
        }

        return rs > 0 ? true : false;
    }


    public <Bean> Boolean batchInsert(List<Bean> beans) throws HaemetaException {
        if (beans == null || beans.size() < 1) {
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        int rs = 0;

        DtoInfo info = DtoCache.getDtoInfo(beans.get(0));

        Map<String, Object> paramMap = notNullValues(beans.get(0),info);
        //SQL: INSERT INTO  XX(
        StringBuffer sql = new StringBuffer("INSERT INTO ").append(info.getTableName()).append("(");
        //SQL-VALUES:  VALUES(
        StringBuffer value = new StringBuffer(" VALUES");


        List<String> keyList = new ArrayList();

        //SQL: INSERT INTO  XX(AA,BB,CC
        int keyIndex = 0;
        Set<String> keys = paramMap.keySet();
        for (String key : keys) {
            sql.append(key);
            keyList.add(key);
            ++keyIndex;
            if (keyIndex != keys.size()) {
                sql.append(",");
            }
        }
        //SQL: INSERT INTO  XX(AA,BB,CC)
        sql.append(")");

        List param = new ArrayList();
        //SQL-VALUES:  VALUES(?,?,?),(?,?,?)
        for (int index = 0; index < beans.size(); index++) {
            Map<String, Object> map = notNullValues(beans.get(index),info);
            value.append("(");
            for (int i = 0; i < keyList.size(); i++) {
                value.append("?");
                param.add(map.get(keyList.get(i)));
                if (i != keyList.size() - 1) {
                    value.append(",");
                }
            }
            value.append(")");
            if (index != beans.size() - 1) {
                value.append(",");
            }
        }


        //SQL: INSERT INTO  XX(AA,BB,CC) VALUES(?,?,?),(?,?,?)
        sql.append(value);

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql.toString());

            //注入参数
            for (int i = 1; i <= param.size(); i++) {
                pstmt.setObject(i, param.get(i - 1));
            }

            //执行SQL
            log.info(pstmt.toString());
            rs = pstmt.executeUpdate();
        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } finally {
            close(conn, pstmt, null);
        }

        return rs > 0 ? true : false;
    }

    public <Bean> Integer insertGetKey(Bean bean) throws HaemetaException {
        if (bean == null) {
            return null;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Integer done = null;
        //最终返回的主键
        Integer generateKey = null;

        DtoInfo info = DtoCache.getDtoInfo(bean);

        Map<String, Object> paramMap = notNullValues(bean,info);

        //SQL: INSERT INTO  XX(
        StringBuffer sql = new StringBuffer("INSERT INTO `").append(info.getTableName()).append("`(");
        //SQL-VALUES:  VALUES(
        StringBuffer value = new StringBuffer(" VALUES(");

        List param = new ArrayList();

        //SQL: INSERT INTO  XX(AA,BB,CC
        int keyIndex = 0;
        Set<String> keys = paramMap.keySet();
        for (String key : keys) {
            sql.append("`").append(key).append("`");
            value.append("?");
            param.add(paramMap.get(key));
            ++keyIndex;
            if (keyIndex != keys.size()) {
                sql.append(",");
                value.append(",");
            }
        }
        //SQL: INSERT INTO  XX(AA,BB) VALUES(?,?)
        sql.append(")").append(value).append(")");

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

            //注入参数
            for (int i = 1; i <= param.size(); i++) {
                pstmt.setObject(i, param.get(i - 1));
            }

            log.info(pstmt.toString());
            //执行SQL
            done = pstmt.executeUpdate();
            if (done > 0) {
                rs = pstmt.getGeneratedKeys();
            } else {
                return null;
            }
            while (rs.next()) {
                generateKey = rs.getInt(1);
            }

        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } finally {
            close(conn, pstmt, null);
        }

        return generateKey;
    }

    public <Bean> Boolean delete(Bean bean) throws HaemetaException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rs = 0;

        try {
            SQL sql = getDeleteSQL(bean);
            conn = getConnection();

            pstmt = conn.prepareStatement(sql.getSql());
            setParams(pstmt, sql.getParameters());
            log.info(pstmt.toString());
            rs = pstmt.executeUpdate();

        } catch (SQLException throwables) {
            throw new HaemetaException(throwables);
        } catch (Exception ex) {
            throw new HaemetaException(ex);
        } finally {
            close(conn, pstmt, null);
        }

        return rs > 0 ? true : false;
    }

    public <Bean, Key> void update(List<Bean> beans, String keyName, Function<Bean, ? super Key> key) throws HaemetaException {
        if (CollectionUtils.isEmpty(beans)) return;

        DtoInfo info = DtoCache.getDtoInfo(beans.get(0));


        String tableName = info.getTableName();
        StringBuffer sqlBuffer = new StringBuffer();
        List param = new ArrayList();

        beans.forEach(bean -> {
            Object unknownKey = key.apply((Bean) bean);
            if (Objects.isNull(unknownKey)) return;

            //UPDATE `XX` SET
            sqlBuffer.append("UPDATE `").append(tableName).append("` SET ");
            Map<String, Object> valueMap = notNullValues(bean,info);
            valueMap.keySet().forEach(keyStr -> {
                //UPDATE `XX` SET `XX`=?,
                sqlBuffer.append(" `").append(keyStr).append("`=").append("?,");
                param.add(valueMap.get(keyStr));
            });
            //UPDATE `XX` SET 'XX'=?,`XX`=?
            if (sqlBuffer.charAt(sqlBuffer.length() - 1) == ',')
                sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
            //UPDATE `XX` SET 'XX'=?,`XX`=? WHERE `XX`=xx;
            sqlBuffer.append(" WHERE `").append(keyName).append("`=? LIMIT 1;");
            param.add(unknownKey);
        });
        String sql = sqlBuffer.toString();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {

            conn = getConnection();

            pstmt = conn.prepareStatement(sql);
            setParams(pstmt, param, true);
            pstmt.execute();

        } catch (Exception e) {
            throw new HaemetaException(e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    /**
     * 批量保存
     * 保存条件：除 id 字段，其他字段必须保持一致
     * @param beans
     * @return
     * @throws HaemetaException
     */
    public <Bean> void save(Bean... beans) throws HaemetaException {
        save(false, beans);
    }

    public <Bean> void save(Boolean usingMaxColumns, Bean... beans) throws HaemetaException {
        SQL sql = getSaveSql(usingMaxColumns, beans);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {

            conn = getConnection();

            pstmt = conn.prepareStatement(sql.getSql());
            setParams(pstmt, sql.getParameters(), true);
            pstmt.execute();

        } catch (Exception e) {
            throw new HaemetaException(e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    /**
     * 批量保存，并返回主键，自动设置
     * 保存条件：除 id 字段，其他字段必须保持一致
     * @param beans
     * @return
     * @throws HaemetaException
     */
    public <T, ID,Bean> void saveGetKey(BiConsumer<Bean, ID> setKeyMethod, Bean... beans) throws HaemetaException {
        saveGetKey(false, setKeyMethod, ListUtil.asList(beans));
    }

    /**
     * 新批量保存
     * 根据主键分离 插入 和 更新 的数据
     * @param list     批量目标    必须实现Bean接口
     * @param getIDKey 获取主键
     * @param setId    设置主键
     * @param <T>
     * @param <ID>
     */
    public <T, ID,Bean> void batchSaveGetKey(List<T> list, Function<T, ID> getIDKey, BiConsumer<T, ID> setId) {
        batchSaveGetKey(list, getIDKey, setId, null, null);
    }

    /**
     * 新批量保存
     * 根据主键分离 插入 和 更新 的数据
     * @param list     批量目标    必须实现Bean接口
     * @param getIDKey 获取主键
     * @param setId    设置主键
     * @param <T>
     * @param <ID>
     */
    public <T, ID,Bean> void batchSaveGetKey(List<T> list, Function<T, ID> getIDKey, BiConsumer<T,ID> setId, Consumer<List<T>> insertCallBack) {
        batchSaveGetKey(list, getIDKey, setId, null, null, insertCallBack, null);
    }

    /**
     * 新批量保存
     * 根据主键分离 插入 和 更新 的数据
     * @param <T>
     * @param <ID>
     */
    public <T, ID,Bean> void batchSaveGetKey(List<T> list, Function<T, ID> getIDKey, BiConsumer<T, ID> setId, Consumer<List<T>> beforeInsertCall, Consumer<List<T>> afterInsert) {
        batchSaveGetKey(list, getIDKey, setId, beforeInsertCall, null, afterInsert, null);
    }

    /**
     * 新批量保存
     * 根据主键分离 插入 和 更新 的数据
     * @param list                批量目标    必须实现Bean接口
     * @param getIDKey            获取主键
     * @param setId               设置主键
     * @param beforeInsertCall    插入数据前对 insertList 的操作
     * @param beforeUpdateCall    更新数据前对 updateList 的操作
     * @param afterInsertCallBack 插入数据后的insertList回调 (传入 List)
     * @param afterUpdateCallBack 更新数据后的回调 (传入 List)
     * @param <T>
     * @param <ID>
     */
    public <T, ID,Bean> void batchSaveGetKey(
            List<T> list,
            Function<T, ID> getIDKey,
            BiConsumer<T, ID> setId,
            Consumer<List<T>> beforeInsertCall,
            Consumer<List<T>> beforeUpdateCall,
            Consumer<List<T>> afterInsertCallBack,
            Consumer<List<T>> afterUpdateCallBack
    ) {
        List<T> insert = new ArrayList<>();
        List<T> update = new ArrayList<>();

        ListUtil.foreach(list, t -> {
            if (Objects.isNull(getIDKey.apply(t)))
                insert.add(t);
            else
                update.add(t);
        });

        if (!CollectionUtils.isEmpty(insert)) {
            ListUtil.partition(insert, 1000).forEach(inner -> {
                try {
                    saveGetKey(true,setId, insert);
                } catch (HaemetaException e) {
                    e.printStackTrace();
                }
            });
            if (Objects.nonNull(afterInsertCallBack))
                afterInsertCallBack.accept(insert);
        }

        if (!CollectionUtils.isEmpty(update)) {
            ListUtil.partition(update, 1000).forEach(inner -> {
                try {
                    save(update);
                } catch (HaemetaException e) {
                    e.printStackTrace();
                }
            });
            if (Objects.nonNull(afterUpdateCallBack))
                afterUpdateCallBack.accept(update);
        }
    }


    /**
     * 使用此方法务必将有主键和无主键的区分开来
     * 此方法存在重大弊端
     * id 1~1000，删除 id 1000
     * 再次同步数据 1~1000，
     * 返回的id 会虚增增长，id 会增到1999
     * @param usingMaxColumns
     * @param setKeyMethod
     * @param beans
     * @throws HaemetaException
     */
    public <Bean, SetKeyType> void saveGetKey(Boolean usingMaxColumns, BiConsumer<Bean, SetKeyType> setKeyMethod, List<Bean> beans) throws HaemetaException {
        SQL sql = getSaveSql(usingMaxColumns, beans);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {

            conn = getConnection();

            pstmt = conn.prepareStatement(sql.getSql(), Statement.RETURN_GENERATED_KEYS);
            setParams(pstmt, sql.getParameters(), true);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            //映射 设置主键的方法
            int index = 0;
            while (rs.next()){
                Object res = rs.getObject(1);
                if(res instanceof BigInteger){
                    res = ((BigInteger)res).intValue();
                    setKeyMethod.accept((Bean) beans.get(index++), (SetKeyType) res);
                }else if (res instanceof Long){
                    res = ((Long) res).intValue();
                    setKeyMethod.accept((Bean) beans.get(index++),(SetKeyType) res);
                }
            }

        } catch (Exception e) {
            throw new HaemetaException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    /**
     * 通过ThreadConnection获取当前线程的唯一连接
     *
     * @return
     */
    private Connection getConnection() {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        return conn;
    }

    /**
     * 关闭，释放资源
     *
     * @param conn
     * @param ps
     * @param rs
     */
    protected void close(Connection conn, PreparedStatement ps, ResultSet rs) throws HaemetaException {
        this.closeCall.close(conn,ps,rs);
    }


    /**
     * 通过实现Bean的实体类，获取插入SQL语句
     *
     * @param sql
     * @return
     */
    protected <Bean> SQL getInsertSQL(SQL<Bean> sql) {

        Bean bean = sql.getBean();
        if(bean == null) return sql;

        DtoInfo info = DtoCache.getDtoInfo(bean);

        Map<String, Object> paramMap = notNullValues(bean,info);

        //SQL: INSERT INTO  XX(
        StringBuffer sqlBuffer = new StringBuffer("INSERT INTO ").append(info.getTableName()).append("(");
        //SQL-VALUES:  VALUES(
        StringBuffer value = new StringBuffer(" VALUES(");

        List param = new ArrayList();

        //SQL: INSERT INTO  XX(AA,BB,CC
        int keyIndex = 0;
        Set<String> keys = paramMap.keySet();
        for (String key : keys) {
            sqlBuffer.append("`").append(key).append("`");
            value.append("?");
            param.add(paramMap.get(key));
            ++keyIndex;
            if (keyIndex != keys.size()) {
                sqlBuffer.append(",");
                value.append(",");
            }
        }
        //SQL: INSERT INTO  XX(AA,BB) VALUES(?,?)
        sqlBuffer.append(")").append(value).append(")");

        sql.setSql(sqlBuffer.toString());
        sql.setParameters(param);

        return sql;
    }

    protected <Bean> SQL getUpdateSQL(SQL<Bean> sqlBean){
        Bean bean = sqlBean.getBean();

        DtoInfo info = DtoCache.getDtoInfo(bean);

        if(sqlBean.getBean() != null){
            Map<String, Object> paramMap = notNullValues(bean,info);

            //SQL: UPDATE XX SET
            StringBuffer sql = new StringBuffer("UPDATE `").append(info.getTableName()).append("` SET ");
            List param = new ArrayList();

            //SQL: UPDATE XX SET AA=?,BB=?
            Set<String> keys = paramMap.keySet();
            for (String key : keys) {
                if (key.equals("id")) {
                    continue;
                }
                sql.append("`").append(key).append("`").append("=?");
                param.add(paramMap.get(key));
                sql.append(",");
            }
            //SQL: UPDATE XX SET AA=?,BB=? WHERE id=?
            sql.deleteCharAt(sql.length() - 1);
            sql.append(" WHERE id=? ");
            param.add(paramMap.get("id"));

            if(!StringUtil.isEmptyWithOutSpace(sqlBean.getAppendSql())){
                sql.append(sqlBean.getAppendSql());
                param.addAll(sqlBean.getAppendParam());
            }
            sqlBean.setParameters(param);
            sqlBean.setSql(sql.toString());
        }else{
            if(!StringUtil.isEmptyWithOutSpace(sqlBean.getAppendSql())){
                sqlBean.setSql(sqlBean.getSql() + " " + sqlBean.getAppendSql());
                sqlBean.getParameters().addAll(sqlBean.getAppendParam());
            }
        }

        return sqlBean;
    }

    /**
     * 通过实现Bean的实体类，获取删除 SQL语句
     *
     * @param bean
     * @return
     */
    protected <Bean> SQL getDeleteSQL(Bean bean) throws HaemetaException {
        SQL sql = new SQL();

        int reasonCount = 0;
        List params = new ArrayList();

        DtoInfo info = DtoCache.getDtoInfo(bean);
        Map<String, Object> paramsMap = notNullValues(bean,info);
        if (paramsMap.size() == 0) {
            throw new HaemetaException(" 禁止全部删除 ").code(ResultStatusCodes.DATA_ERROR);
        }
        //SQL : DELETE FROM XX
        StringBuffer sqlBuffer = new StringBuffer("DELETE FROM ").append(info.getTableName());
        StringBuffer where = new StringBuffer();
        //SQL : id=?
        if (paramsMap.get("id") != null) {
            where.append("id=?");
            params.add(paramsMap.get("id"));
            ++reasonCount;
        }

        //SQL : id=? AND aa=? AND cc=?
        Set<String> keys = paramsMap.keySet();
        for (String key : keys) {
            if (key.equals("id")) {
                continue;
            }
            if (reasonCount != 0) {
                where.append(" AND ");
            }
            where.append(key).append("=?");
            params.add(paramsMap.get(key));
            ++reasonCount;
        }

        //SQL : DELETE FROM XX WHERE id=? AND aa=? AND cc=?
        if (reasonCount != 0) {
            sqlBuffer.append(" WHERE ").append(where);
        }

        sql.setSql(sqlBuffer.toString());
        sql.setParameters(params);

        return sql;
    }

    /**
     * 通过Bean的实体类，获取保存语句
     *
     * @param beans
     * @return
     * @throws HaemetaException
     */
    protected <Bean> SQL getSaveSql(Boolean usingMaxColumns, Bean... beans) throws HaemetaException {
        SQL sql = new SQL();
        DtoInfo info = DtoCache.getDtoInfo(beans[0]);


        //最大列数
        Integer maxColumnsSize = 0;
        //最大列数对象的下标
        Integer maxColumnsIndex = 0;
        if (usingMaxColumns) {
            for (int i = 0; i < beans.length; i++) {
                Set temp = notNullValues(beans[0],info).keySet();
                if (temp.size() > maxColumnsSize) {
                    maxColumnsSize = temp.size();
                    maxColumnsIndex = i;
                }
            }
        }

        Bean init = usingMaxColumns ? beans[maxColumnsIndex] : beans[0];
        //不重复 map
        Map keyV = notNullValues(init,info);
        //要插入的参数目标
        Set<String> keys = keyV.keySet();


        //SQL:INSERT INTO XX
        StringBuffer sqlBuffer = new StringBuffer("INSERT INTO ").append(info.getTableName());

        //SQL: ON DUPLICATE KEY UPDATE
        StringBuffer updateBuffer = new StringBuffer(" ON DUPLICATE KEY UPDATE ");

        //SQL:INSERT INTO XX(`id`,`
        sqlBuffer.append("(`id`,");
        //SQL: ON DUPLICATE KEY UPDATE `id`=VALUES(`id`),
//        updateBuffer.append("`id`=VALUES(`id`),");

        //SQL: INSERT INTO XX(`id`,`xx`,`xx`,
        //UPD: ON DUPLICATE KEY UPDATE `id`=VALUES(`id`),`xx`=VALUES(`xx`),`xx`=VALUES(`xx`),
        for (String key : keys) {
            if (key.equals("id")) continue;
            //SQL: INSERT INTO XX(`xx`,
            sqlBuffer.append("`").append(key).append("`").append(",");
            //SQL: ON DUPLICATE KEY UPDATE `xx`=VALUES(`xx`),
            updateBuffer.append("`").append(key).append("`=VALUES(`").append(key).append("`),");
        }
        //SQL: INSERT INTO XX(`id`,`xx`,`xx`)
        if (sqlBuffer.charAt(sqlBuffer.length() - 1) == ',')
            sqlBuffer.deleteCharAt(sqlBuffer.length() - 1).append(") ");
        //SQL: ON DUPLICATE KEY UPDATE `id`=VALUES(`id`),`xx`=VALUES(`xx`),`xx`=VALUES(`xx`);
        if (updateBuffer.charAt(updateBuffer.length() - 1) == ',')
            updateBuffer.deleteCharAt(updateBuffer.length() - 1).append(";");

        //INSERT INTO XX(`id`,`xx`,`xx`) VALUES
        sqlBuffer.append("VALUES");

        List param = new ArrayList();

        //INSERT INTO XX(`id`,`xx`,`xx`) VALUES(?,?,?),(?,?,?),
        for (Bean bean : beans) {
            Map<String, Object> paramMap = notNullValues(bean,info);

            //id 的参数
            //INSERT INTO XX(`id`,`xx`,`xx`) VALUES(?,
            sqlBuffer.append("(?,");
            param.add(paramMap.get("id"));

            //INSERT INTO XX(`xx`,`xx`) VALUES(?,?,?,
            for (String key : keys) {
                if (key.equals("id")) continue;
                //INSERT INTO XX(`xx`,`xx`) VALUES(?,?,?,
                sqlBuffer.append("?,");
                //添加参数
                param.add(paramMap.get(key));
            }

            //去除最后一个 ',' 添加 '),'
            //INSERT INTO XX(`xx`,`xx`) VALUES(?,?,?),
            if (sqlBuffer.charAt(sqlBuffer.length() - 1) == ',')
                sqlBuffer.deleteCharAt(sqlBuffer.length() - 1).append("),");

        }

        //去除最后一个 ','
        //INSERT INTO XX(`id`,`xx`,`xx`) VALUES(?,?,?),(?,?,?)
        if (sqlBuffer.charAt(sqlBuffer.length() - 1) == ',')
            sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);


        //SQL 最后拼接
        //INSERT INTO XX(`id`,`xx`,`xx`) VALUES(?,?,?),(?,?,?) ON DUPLICATE KEY UPDATE `id`=VALUES(`id`),`xx`=VALUES(`xx`),`xx`=VALUES(`xx`);
        sqlBuffer.append(updateBuffer);

        //INSERT INTO XX(`xx`,`xx`) VALUES(?,?) ON DUPLICATE KEY UPDATE `xx`=values(`xx`),`xx`=values(`xx`);
        sql.setSql(
                sqlBuffer.toString()
        );
        sql.setParameters(param);
        return sql;
    }

    /**
     * 从 结果集中 取出 结果，以list返回
     *
     * @param rs
     * @return
     */
    protected List getFromResultSet(ResultSet rs) {
        List list = new ArrayList();
        ResultSetMetaData rsmd = null;
        try {
            rsmd = rs.getMetaData();
            while (rs.next()) {
                HashMapDto map = new HashMapDto();
                //循环出每行
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    try {
                        String columnName = rsmd.getColumnLabel(i);
                        map.put(columnName, rs.getObject(i));
                    } catch (Exception ex) {
                        map.put(rsmd.getColumnName(i), "Data Error");
                    }
                }
                list.add(map);
            }
        } catch (SQLException throwables) {
            log.error(getClass() + ": 【 method : protected List getFromResultSet(ResultSet) 】 failed;");
        }
        return list;
    }

    /**
     * 从 结果集中 取出 结果，以Map返回
     * key 为指定的 key
     *
     * @param rs
     * @return
     */
    protected Map getFromResultSet_MAP_RESULT(ResultSet rs, String key) {
        Map res = new HashMap();
        ResultSetMetaData rsmd = null;
        try {
            rsmd = rs.getMetaData();
            while (rs.next()) {
                HashMapDto map = new HashMapDto(true);
                //循环出每行
                Object keyValue = null;
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    try {
                        String columnName = rsmd.getColumnLabel(i);
                        Object columnValue = rs.getObject(i);
                        if (columnName.equals(key))
                            keyValue = columnValue;
                        if (keyValue == null) continue;
                        map.put(columnName, columnValue);
                    } catch (Exception ex) {
                        map.put(rsmd.getColumnName(i), "Data Error");
                    }
                }
                if (keyValue != null) res.put(keyValue, map);
            }
        } catch (SQLException throwables) {
            log.error(getClass() + ": 【 method : protected List getFromResultSet(ResultSet) 】 failed;");
        }
        return res;
    }

    /**
     * 内部方法，快速执行SQL
     *
     * @param pstmt
     * @param param
     * @return
     * @throws SQLException
     */
    protected ResultSet executeQuery(PreparedStatement pstmt, List param) throws SQLException {

        if (param != null) {
            setParams(pstmt, param);
        }

        log.info(pstmt.toString());
        ResultSet rs = pstmt.executeQuery();

        return rs;
    }

    /**
     * 默认不使用空值做参的
     *
     * @param pstmt
     * @param params
     */
    protected void setParams(PreparedStatement pstmt, List params) {
        setParams(pstmt, params, false);
    }

    /**
     * @param pstmt
     * @param params
     * @param nullValue 是否空值
     */
    protected void setParams(PreparedStatement pstmt, List params, boolean nullValue) {
        try {
            if (params.size() != 0)
                for (int i = 0; i < params.size(); i++) {

                    if (params.get(i) == null && nullValue) {
                        pstmt.setObject(i + 1, null);
                        continue;
                    }

                    String clazzStr = params.get(i).getClass().toString();
                    if (clazzStr.equals(Date.class.toString())) {
                        Date ud = (Date) params.get(i);
                        Long times = ud.getTime();
                        java.sql.Date sd = new java.sql.Date(times);
//                        pstmt.setDate(i + 1, sd);
                        pstmt.setTimestamp(i + 1, new Timestamp(ud.getTime()));
                    } else if (clazzStr.equals(java.sql.Date.class.toString())) {
                        pstmt.setDate(i + 1, (java.sql.Date) params.get(i));
                    } else if (clazzStr.equals(BigDecimal.class.toString())) {
                        pstmt.setBigDecimal(i + 1, (BigDecimal) params.get(i));
                    } else if (clazzStr.equals(Boolean.class.toString())) {
                        pstmt.setBoolean(i + 1, (Boolean) params.get(i));
                    } else {
                        pstmt.setObject(i + 1, params.get(i));
                    }
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected <Bean> Map<String,Object> notNullValues(Bean bean,DtoInfo info){
        Map<String, Method> getMethods = info.getGetMethods();
        Map<String,String> columnsByFields = info.getColumns();
        Map<String,Object> values = new LinkedHashMap<>();
        info.getFields().forEach(field -> {
            try {
                Object value = getMethods.get(field).invoke(bean);
                if(value != null)
                    values.put(columnsByFields.get(field),value);
            } catch (Exception e) {
                return;
            }
        });
        return values;
    }

    public static interface CloseLambda{
        void close(Connection conn, PreparedStatement ps, ResultSet rs) throws HaemetaException;
    }

}
