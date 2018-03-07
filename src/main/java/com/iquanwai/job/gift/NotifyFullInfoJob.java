package com.iquanwai.job.gift;

import com.iquanwai.domain.GiftService;
import com.iquanwai.util.cat.CatInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每周5导入礼包寄送名单，对信息不完整的人发送补全信息模板消息
 */
@Component
public class NotifyFullInfoJob {
    @Autowired
    private GiftService giftService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 0 17 ? * FRI")
    @CatInspect(name = "notifyFullInfo")
    public void work() {
        logger.info("周五统计寄送名单并发送补全信息模板消息开始");
        //TODO:排除节假日
        //生成寄送名单并发送模板消息
        giftService.generateList();
        logger.info("统计寄送名单并发送模板消息结束");
    }



}
