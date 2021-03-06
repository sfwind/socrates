package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.QuanwaiOrder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/1/19.
 */
@Repository
public class QuanwaiOrderDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<QuanwaiOrder> queryWechatCloseOrders(Date openTime) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiOrder>> h = new BeanListHandler<>(QuanwaiOrder.class);

        try {
            List<QuanwaiOrder> orderList = run.query("SELECT * FROM QuanwaiOrder where Status=0 and createTime<=? and (PayType = 1 or PayType is null) ",
                    h, openTime);
            return orderList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<QuanwaiOrder> queryAliCloseOrders(Date openTime) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuanwaiOrder>> h = new BeanListHandler<>(QuanwaiOrder.class);

        try {
            List<QuanwaiOrder> orderList = run.query("SELECT * FROM QuanwaiOrder where Status=0 and createTime<=? and PayType = 2",
                    h, openTime);
            return orderList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public QuanwaiOrder loadOrder(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<QuanwaiOrder> h = new BeanHandler<>(QuanwaiOrder.class);

        try {
            QuanwaiOrder order = run.query("SELECT * FROM QuanwaiOrder where OrderId=? ", h, orderId);
            return order;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void closeOrder(String orderId) {
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE QuanwaiOrder SET Status=2 " +
                    "where OrderId=?", orderId);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
