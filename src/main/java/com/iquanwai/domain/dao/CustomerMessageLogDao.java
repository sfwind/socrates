package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.CustomerMessageLog;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/8/10.
 */
@Repository
public class CustomerMessageLogDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(CustomerMessageLog messageLog) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CustomerMessageLog (Openid, PublishTime, Comment, ContentHash, ForwardlyPush, ValidPush) " +
                "VALUES ( ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    messageLog.getOpenId(),
                    messageLog.getPublishTime(),
                    messageLog.getComment(),
                    messageLog.getContentHash(),
                    messageLog.getForwardlyPush(),
                    messageLog.getValidPush());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<CustomerMessageLog> loadByOpenId(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM CustomerMessageLog WHERE OpenId = ? AND ValidPush = 1";
        ResultSetHandler<List<CustomerMessageLog>> h = new BeanListHandler<>(CustomerMessageLog.class);
        try {
            return runner.query(sql, h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
