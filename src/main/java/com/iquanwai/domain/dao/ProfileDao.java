package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.Profile;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Boolean riseMemberExpired(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update Profile set RiseMember = 0 where Id = ?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    // 根据入库日期筛选 profile
    public List<Profile> loadProfiles(Date addTime) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Profile WHERE AddTime = ?";
        ResultSetHandler<List<Profile>> h = new BeanListHandler<>(Profile.class);
        try {
            return runner.query(sql, h, addTime);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
