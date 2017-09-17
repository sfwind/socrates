package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/8/10.
 */
@Data
public class CustomerMessageLog {
    private Integer id;
    private String openid;
    private Date publishTime;
    private String comment;
}
