package com.iquanwai.job;

import com.iquanwai.domain.ForumService;
import com.iquanwai.domain.MessageService;
import com.iquanwai.domain.po.AnswerApproval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by justin on 17/6/27.
 */
@Component
public class NotifyForumJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MessageService messageService;
    @Autowired
    private ForumService forumService;

    @Scheduled(cron = "0 0 0/4 * * ?")
    public void work() {
        logger.info("ForumNotifyJob start");
        //发送点赞数消息
        List<AnswerApproval> answerApprovalList = forumService.getAnswerApprovals();
        messageService.sendForumLikeMessage(answerApprovalList);

        logger.info("ForumNotifyJob end");
    }
}
