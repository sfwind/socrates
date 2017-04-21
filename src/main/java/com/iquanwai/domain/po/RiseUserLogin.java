package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/4/21.
 * 用户每日登录记录
 */
@Data
public class RiseUserLogin {
    private Integer id;
    private String openid;
    private Date LoginDate;
    private Integer diffDay;
    private Date addTime;
    private Date updateTime;
}
