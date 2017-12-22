package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by 三十文 on 2017/10/13
 */
@Repository
public class CustomerStatusDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Integer insert(Integer profileId, Integer statusId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CustomerStatus(ProfileId, StatusId, Del) VALUES(?,?,0)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), profileId, statusId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
