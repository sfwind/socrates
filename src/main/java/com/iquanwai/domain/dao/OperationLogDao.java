package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.OperationLog;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class OperationLogDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(OperationLog log){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
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

    public List<String> loadThatDayLoginUser(Integer days){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select DISTINCT Openid from OperatingLog where OperateDate = DATE_SUB(CURDATE(),INTERVAL ? DAY) " +
                "                and ((Function = '开始训练' AND Action = '加载训练') OR " +
                "                     (Function = '挑战训练' AND (Action = 'PC加载挑战训练' OR Action = '挑战训练列表加载自己的')) OR " +
                "                     (Function = '应用任务' AND (Action = '应用任务列表加载自己的应用任务' OR Action = '加载自己的应用任务')) OR " +
                "                     (Module = '打点') OR" +
                "                     (Action = 'PC端加载小课论坛')) ";

        try{
            ResultSetHandler<List<String>> handler = new ColumnListHandler<String>();
            return runner.query(sql, handler, days);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


}
