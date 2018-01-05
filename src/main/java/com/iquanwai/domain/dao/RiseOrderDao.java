package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/4/6.
 */
@Repository
public class RiseOrderDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void closeOrder(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Update RiseOrder set Del=1 where OrderId=?";
        try {
            run.update(sql, orderId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
