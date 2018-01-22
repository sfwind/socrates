package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/9/14.
 */
@Data
public class Coupon {

    private Integer id;
    private Integer profileId;
    private Integer amount; // 优惠券金额
    private Integer used; // 是否使用 0-否 1-是
    private String orderId; // 用于订单的 id
    private Integer cost; // 本次订单已消耗的金额
    private Date expiredDate; // 过期日期
    private String category; // 分类
    private String description; // 描述

    /**
     * 非 db 字段
     */
    private String expired; // 过期时间 非DB字段
    private String expiredDateString; // 过期时间 string

    //已使用
    public static final int USED = 1;
    //未使用
    public static final int UNUSED = 0;
    //正在使用
    public static final int USING = 2;

    public interface Category {
        String ELITE_RISE_MEMBER = "ELITE_RISE_MEMBER";
        String OFF_LINE_WORKSHOP = "OFF_LINE_WORKSHOP";
    }

}
