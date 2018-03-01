package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.RiseUserLogin;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/4/21.
 */
@Repository
public class RiseUserLoginDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean insert(Integer profileId, Date loginDate, Integer diffDay) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseUserLogin(ProfileId, LoginDate, DiffDay) VALUES (?, ?, ?) ";
        long insert = 0;
        try {
            ScalarHandler<Long> handler = new ScalarHandler<Long>();
            insert = runner.insert(sql, handler, profileId, loginDate, diffDay);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return insert > 0;
    }

    public RiseUserLogin loadCertainLogin(Integer profileId,Date loginDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseUserLogin WHERE ProfileId = ? and LoginDate = ?";
        try {
            return runner.query(sql, new BeanHandler<RiseUserLogin>(RiseUserLogin.class), profileId, loginDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    // 获取超过 x 天未登录的学员
    public List<RiseUserLogin> loadUnLoginUser(String previousLoginDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select ProfileId, max(LoginDate) as LoginDate " +
                "from RiseUserLogin GROUP BY ProfileId having max(LoginDate) < ?";
        ResultSetHandler<List<RiseUserLogin>> h = new BeanListHandler<>(RiseUserLogin.class);
        try {
            return runner.query(sql, h, previousLoginDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
