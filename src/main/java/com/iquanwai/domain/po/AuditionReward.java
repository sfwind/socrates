package com.iquanwai.domain.po;

import lombok.Data;

@Data
public class AuditionReward {

    private Integer id;
    private Integer auditionId;
    private Integer profileId;
    private Integer identity;
    private String className;
    private String groupId;
    private Boolean del;

    public interface Identity {
        int NORMAL = 0;
        int COMMITTEE = 1; // 班委
        int WINNINGGROUP = 2; // 优胜小组
    }

}
