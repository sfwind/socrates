package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 2017/11/30.
 */
@Repository
public class BusinessSchoolApplicationOrderDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void closeOrder(String orderId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update BusinessSchoolApplicationOrder SET Del = 1 WHERE OrderId = ?";
        try {
            runner.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
