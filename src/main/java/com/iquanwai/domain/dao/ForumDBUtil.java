package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DBProperties;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
@Repository
public class ForumDBUtil {
    private ComboPooledDataSource ds;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    @PostConstruct
    public DataSource getDataSource(){
        if(ds==null) {
            ds = new ComboPooledDataSource();
            ds.setPassword(ConfigUtils.getPassword());
            ds.setUser(ConfigUtils.getUsername());
            ds.setJdbcUrl(ConfigUtils.getForumJdbcUrl());
            try {
                ds.setDriverClass("com.mysql.jdbc.Driver");
            } catch (PropertyVetoException e) {
                // ignore
            }
            ds.setCheckoutTimeout(DBProperties.CHECKOUT_TIMEOUT);
            ds.setMaxStatements(DBProperties.MAX_STATEMENTS);
            ds.setMaxPoolSize(DBProperties.MAX_POOL_SIZE);
            ds.setMinPoolSize(DBProperties.MIN_POOL_SIZE);
            ds.setMaxIdleTime(DBProperties.MAX_IDLE_TIME);
            ds.setIdleConnectionTestPeriod(DBProperties.IDLE_CONNECTION_TEST_PERIOD);
        }
        return ds;
    }

    public <T> T load(Class<T> type, Integer id){
        if(id==null){
            return null;
        }
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<T> h = new BeanHandler<T>(type);

        try {
            T t = run.query("SELECT * FROM "+type.getSimpleName()+" where id=?", h, id);
            return t;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public <T> List<T> loadAll(Class<T> type){

        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<T>> h = new BeanListHandler<T>(type);

        try {
            List<T> t = run.query("SELECT * FROM "+type.getSimpleName()+" limit 10000", h);
            return t;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public long count(Class type){

        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            Long number = run.query("SELECT count(*) FROM "+type.getSimpleName(), h);
            return number;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1L;
    }

    protected String produceQuestionMark(int size){
        if(size==0){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<size;i++){
            sb.append("?,");
        }

        return sb.deleteCharAt(sb.length()-1).toString();
    }
}
