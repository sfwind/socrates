package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/4/21.
 * 用户第一次登录记录
 */
@Data
public class RiseUserLanding {
    private int id;
    private Integer profileId;
    private Date landingDate;
    private Date addTime;
    private Date updateTime;
}
