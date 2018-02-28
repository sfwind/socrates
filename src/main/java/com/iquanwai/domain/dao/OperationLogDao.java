package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.OperationLog;
import com.iquanwai.util.ThreadPool;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class OperationLogDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(OperationLog log){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        try {
            String insertSql = "INSERT INTO OperatingLog(Openid, Module, Function, Action, OperateTime, OperateDate, Memo) " +
                    "VALUES(?, ?, ?, ?, now(), curdate(), ?)";
            Future<Integer> result =  asyncRun.update(insertSql,
                    log.getOpenid(), log.getModule(), log.getFunction(),
                    log.getAction(), log.getMemo());
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

    public List<Integer> loadThatDayLoginUser(Integer days){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select DISTINCT ProfileId from ActionLog where DATE(AddTime) = DATE_SUB(CURDATE(),INTERVAL ? DAY) ";

        try{
            ResultSetHandler<List<Integer>> handler = new ColumnListHandler<>();
            return runner.query(sql, handler, days);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


}
