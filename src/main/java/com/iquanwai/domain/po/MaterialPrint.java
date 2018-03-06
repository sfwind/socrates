package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

@Data
public class MaterialPrint {
    private Integer id;
    private Integer profileId;
    private String type;
    private Integer posted;
    /**
     * 校验批次
     */
    private Date checkBatch;
    /**
     * 发送批次
     */
    private Date sendBatch;

    private Integer del;

}
