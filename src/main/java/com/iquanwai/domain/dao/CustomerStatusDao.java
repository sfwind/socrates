package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.CustomerStatus;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文 on 2017/10/13
 */
@Repository
public class CustomerStatusDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<CustomerStatus> loadCustomerStatusByAddTime(String addTime, Integer statusId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CustomerStatus WHERE date(AddTime) = ? AND StatusId = ? AND Del = 0";
        ResultSetHandler<List<CustomerStatus>> h = new BeanListHandler<>(CustomerStatus.class);
        try {
            return runner.query(sql, h, addTime, statusId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
