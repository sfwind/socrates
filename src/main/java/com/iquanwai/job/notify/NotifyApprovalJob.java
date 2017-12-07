package com.iquanwai.job.notify;

import com.iquanwai.domain.MessageService;
import com.iquanwai.domain.PracticeService;
import com.iquanwai.domain.po.HomeworkVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by justin on 17/3/1.
 */
@Component
public class NotifyApprovalJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private MessageService messageService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void work() {
        logger.info("点赞消息任务开始");
        //发送点赞数统计
        likeMessage();
        logger.info("点赞消息任务结束");
    }


    private void likeMessage() {
        List<HomeworkVote> homeworkVotes = practiceService.loadVoteYesterday();

        messageService.sendLikeMessage(homeworkVotes);
    }
}
