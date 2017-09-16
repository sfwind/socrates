package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.CustomerMessageLog;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Created by nethunder on 2017/8/10.
 */
@Repository
public class CustomerMessageLogDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void insert(CustomerMessageLog customerMessageLog) {
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncQueryRunner = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "INSERT INTO CustomerMessageLog(Openid, PublishTime, Comment) VALUES(?,?,?)";
        try {
            asyncQueryRunner.insert(sql, new ScalarHandler<Long>(),
                    customerMessageLog.getOpenid(),
                    customerMessageLog.getPublishTime(),
                    customerMessageLog.getComment());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
