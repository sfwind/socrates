package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.RiseUserLanding;
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
public class RiseUserLandingDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RiseUserLanding loadByProfileId(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseUserLanding where ProfileId = ?";
        try{
            ResultSetHandler<RiseUserLanding> handler = new BeanHandler<>(RiseUserLanding.class);
            return runner.query(sql, handler, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public boolean insert(Integer profileId, Date landingDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseUserLanding(ProfileId, LandingDate) VALUES (?,?) ";
        long result = 0;
        try{
            ScalarHandler<Long> handler = new ScalarHandler<Long>();
            result = runner.insert(sql, handler, profileId, landingDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return result > 0;
    }

    public boolean update(Integer profileId, int id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update RiseUserLanding set ProfileId=? where id=?";
        long result = 0;
        try{
            ScalarHandler<Long> handler = new ScalarHandler<Long>();
            result = runner.update(sql, handler, profileId, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return result > 0;
    }

    public List<RiseUserLanding> selectAll() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseUserLanding where ProfileId is null limit 10000";
        try{
            ResultSetHandler<List<RiseUserLanding>> handler = new BeanListHandler<>(RiseUserLanding.class);
            return runner.query(sql, handler);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
