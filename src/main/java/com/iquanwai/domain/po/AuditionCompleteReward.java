package com.iquanwai.domain.po;

import lombok.Data;

@Data
public class AuditionCompleteReward {

    private Integer id;
    private Integer profileId;
    private String className;
    private Double amount;
    private Boolean notified;
    private Boolean del;

}
