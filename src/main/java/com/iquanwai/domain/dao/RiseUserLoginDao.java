package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by nethunder on 2017/4/21.
 */
@Repository
public class RiseUserLoginDao extends DBUtil {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public boolean insert(String openId, Date loginDate, Integer diffDay) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseUserLogin(Openid, LoginDate, DiffDay) VALUES (?, ?, ?) ";
        long insert=0;
        try{
            ScalarHandler<Long> handler = new ScalarHandler<Long>();
            insert = runner.insert(sql, handler, openId, loginDate, diffDay);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return insert > 0;
    }
}
