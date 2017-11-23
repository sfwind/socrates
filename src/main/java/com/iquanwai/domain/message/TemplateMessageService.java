package com.iquanwai.domain.message;

/**
 * Created by justin on 16/8/10.
 */
public interface TemplateMessageService {
    /**
     * 发送强制推送类模板消息，遵循模板发送规范
     * @return 返回发送结果
     */
    boolean sendMessage(TemplateMessage templateMessage);

    String SEND_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={access_token}";

    boolean sendMessage(TemplateMessage templateMessage, boolean forwardlyPush);
}
