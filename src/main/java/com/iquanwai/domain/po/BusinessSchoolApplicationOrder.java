package com.iquanwai.domain.po;

import lombok.Data;

/**
 * Created by justin on 2017/11/30.
 */
@Data
public class BusinessSchoolApplicationOrder {
    private Integer id;
    private Integer profileId;
    private String orderId;
    private Boolean paid;
    private Boolean del;
}
