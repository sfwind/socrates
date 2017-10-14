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

}
