package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文 on 2017/10/13
 */
@Data
public class CustomerStatus {

    private Integer id;
    private Integer profileId;
    private Integer statusId;
    private Integer del;
    private Date addTime;
    private Date updateTime;

    public static final Integer OPEN_BIBLE = 1; //开bible
    public static final Integer EDIT_TAG = 2; //选择tag
    public static final Integer APPLY_BUSINESS_SCHOOL_SUCCESS = 3; // 申请通过

}
