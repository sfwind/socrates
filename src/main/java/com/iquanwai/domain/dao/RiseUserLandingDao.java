package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.RiseUserLanding;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by nethunder on 2017/4/21.
 */
@Repository
public class RiseUserLandingDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RiseUserLanding loadByOpenId(String openid){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseUserLanding where Openid = ?";
        try{
            ResultSetHandler<RiseUserLanding> handler = new BeanHandler<RiseUserLanding>(RiseUserLanding.class);
            return runner.query(sql, handler, openid);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public boolean insert(String openId, Date landingDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseUserLanding(Openid, LandingDate) VALUES (?,?) ";
        long result = 0;
        try{
            ScalarHandler<Long> handler = new ScalarHandler<Long>();
            result = runner.insert(sql, handler, openId, landingDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return result > 0;
    }
}
