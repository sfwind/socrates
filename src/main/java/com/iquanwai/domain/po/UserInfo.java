package com.iquanwai.domain.po;

import lombok.Data;

@Data
public class UserInfo {

    /**
     * 用户id
     */
    private Integer profileId;

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 收件人
     */
    private String receiver;
    /**
     * 收件人手机号
     */
    private String receiverMobile;
    /**
     * 地址
     */
    private String address;


}
