package com.iquanwai.domain.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DBProperties;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private DruidDataSource ds;

    @Bean
    @PostConstruct
    public DataSource getDataSource() {
        if (ds == null) {
            ds = new DruidDataSource();
            ds.setUrl(ConfigUtils.getFragmentJdbcUrl());
            ds.setUsername(ConfigUtils.getUsername());
            ds.setPassword(ConfigUtils.getPassword());
            ds.setInitialSize(DBProperties.INITIAL_SIZE);
            ds.setMinIdle(DBProperties.MIN_IDLE);
            ds.setMaxActive(DBProperties.MAX_ACTIVE);
            ds.setMaxWait(DBProperties.MAX_WAIT);
            ds.setTimeBetweenEvictionRunsMillis(DBProperties.TIME_BETWEEN_EVICTION_RUNS_MILLIS);
            ds.setMinEvictableIdleTimeMillis(DBProperties.MIN_EVICTABLE_IDLE_TIME_MILLIS);
            ds.setValidationQuery(DBProperties.VALIDATION_QUERY);
            ds.setTestWhileIdle(DBProperties.TEST_WHILE_IDLE);
            ds.setTestOnBorrow(DBProperties.TEST_ON_BORROW);
            ds.setTestOnReturn(DBProperties.TEST_ON_RETURN);
            ds.setQueryTimeout(DBProperties.QUERY_TIMEOUT);
            ds.setTransactionQueryTimeout(DBProperties.TRANSACTION_QUERY_TIMEOUT);
            ds.setLoginTimeout(DBProperties.LOGIN_TIMEOUNT);
            ds.setPoolPreparedStatements(DBProperties.POOL_PREPARED_STATEMENTS);
        }
        return ds;
    }


    public <T> T load(Class<T> type, int id) {

        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<T> h = new BeanHandler<T>(type);

        try {
            T t = run.query("SELECT * FROM " + type.getSimpleName() + " where id=?", h, id);
            return t;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public <T> List<T> loadAll(Class<T> type) {

        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<T>> h = new BeanListHandler<T>(type);

        try {
            List<T> t = run.query("SELECT * FROM " + type.getSimpleName() + " limit 10000", h);
            return t;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public long count(Class type) {

        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();

        try {
            Long number = run.query("SELECT count(*) FROM " + type.getSimpleName(), h);
            return number;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1L;
    }

    protected String produceQuestionMark(int size) {
        if (size == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append("?,");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
