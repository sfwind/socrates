package com.iquanwai.domain.dao;

import com.iquanwai.mq.MQSendLog;
import com.iquanwai.util.ThreadPool;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by nethunder on 2017/7/24.
 */
@Repository
public class MQSendLogDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(MQSendLog message){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        try {
            String insertSql = "INSERT INTO MQSendLog(MsgId,PublisherIp ,Topic, Message,SendError) VALUES (?,?,?,?,?)";
            Future<Integer> result = asyncRun.update(insertSql, message.getMsgId(), message.getPublisherIp(),
                    message.getTopic(), message.getMessage(), message.getSendError());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }
}
