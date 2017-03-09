package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.NotifyMessage;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/2/27.
 */
@Repository
public class NotifyMessageDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void insert(NotifyMessage message){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into NotifyMessage(Message, FromUser, ToUser, Url, SendTime, IsRead, Old)" +
                "values(?,?,?,?,?,?,?)";
        try {
            runner.insert(sql, new ScalarHandler<>(),
                    message.getMessage(), message.getFromUser(), message.getToUser(),
                    message.getUrl(), message.getSendTime(), message.getIsRead(),
                    message.getOld());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }
}
