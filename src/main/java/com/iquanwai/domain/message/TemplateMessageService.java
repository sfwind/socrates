package com.iquanwai.domain.message;

/**
 * Created by justin on 16/8/10.
 */
public interface TemplateMessageService {
    /**
     * 发送模板消息
     * @return 返回发送结果
     */
    boolean sendMessage(TemplateMessage templateMessage);

    String SEND_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={access_token}";

    boolean sendMessage(TemplateMessage templateMessage, boolean forwardlyPush);
}
