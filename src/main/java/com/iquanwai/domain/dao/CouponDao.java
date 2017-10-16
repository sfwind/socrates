package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.Coupon;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/9/14.
 */
@Repository
public class CouponDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void updateCouponByOrderId(Integer status, String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE Coupon SET Used =?, OrderId=null, Cost=null " +
                    "where OrderId = ?", status, orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<Coupon> loadCouponsByProfileId(Integer profileId, String category, String description) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Coupon WHERE ProfileId = ? AND Used = 0 AND Category = ? AND Description = ?";
        ResultSetHandler<List<Coupon>> h = new BeanListHandler<>(Coupon.class);
        try {
            return runner.query(sql, h, profileId, category, description);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
