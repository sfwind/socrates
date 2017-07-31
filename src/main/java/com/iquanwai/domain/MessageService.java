package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.iquanwai.domain.dao.NotifyMessageDao;
import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.dao.SubmitDao;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.DateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by justin on 17/2/27.
 */
@Service
public class MessageService {
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private SubmitDao submitDao;
    @Autowired
    private NotifyMessageDao notifyMessageDao;

    private List<Integer> NOTICE_TYPE = Lists.newArrayList(1, 2, 3);

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SYSTEM_MESSAGE = "AUTO";

    public void sendMessage(String message, String toUser, String fromUser, String url) {
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setFromUser(fromUser);
        notifyMessage.setToUser(toUser);
        notifyMessage.setMessage(message);
        notifyMessage.setIsRead(false);
        notifyMessage.setOld(false);
        notifyMessage.setSendTime(DateUtils.parseDateTimeToString(new Date()));
        notifyMessage.setUrl(url);

        notifyMessageDao.insert(notifyMessage);
    }


    public void sendLikeMessage(List<HomeworkVote> homeworkVotes) {
        List<VoteMessage<HomeworkVote>> voteMessageList = Lists.newArrayList();

        //自己给自己点赞不提醒
        homeworkVotes.stream().filter(h1 -> !h1.getVoteOpenId().equals(h1.getVotedOpenid()))
                .filter(h1 -> NOTICE_TYPE.contains(h1.getType()))
                .forEach(homeworkVote -> {
                    VoteMessage<HomeworkVote> voteMessage = new VoteMessage<>(homeworkVote.getReferencedId(),
                            homeworkVote.getType());

                    //如果已经有了记录,点赞数+1
                    if (voteMessageList.contains(voteMessage)) {
                        voteMessageList.forEach(voteMessageInList -> {
                            if (voteMessageInList.equals(voteMessage)) {
                                voteMessageInList.increment();
                            }
                        });
                    } else {
                        //如果已经没有记录,添加记录
                        voteMessageList.add(voteMessage);
                    }
                    voteMessage.setLastVote(homeworkVote);
                });

        //发送消息
        voteMessageList.stream().forEach(voteMessage -> {
            HomeworkVote homeworkVote = voteMessage.getLastVote();
            Integer profileId = homeworkVote.getVoteProfileId();
            Profile profile = profileDao.load(Profile.class, profileId);
            //没查到点赞人,不发消息
            if (profile == null) {
                logger.error("{} is not existed", profileId);
                return;
            }
            String message = getLikeMessage(voteMessage, profile);
            if (StringUtils.isEmpty(message)) {
                logger.error("{} is not supported", voteMessage);
                return;
            }
            String toUser = Objects.toString(homeworkVote.getVotedProfileId());
            String url = "";
            if (voteMessage.getType() == 2) {
                ApplicationSubmit applicationSubmit = submitDao.load(ApplicationSubmit.class, homeworkVote.getReferencedId());
                if (applicationSubmit == null) {
                    return;
                }
                url = "/rise/static/practice/application?id=" + applicationSubmit.getApplicationId()
                        + "&planId=" + applicationSubmit.getPlanId() + "&submitId=" + applicationSubmit.getId();
            } else if (voteMessage.getType() == 3) {
                SubjectArticle subjectArticle = submitDao.load(SubjectArticle.class, homeworkVote.getReferencedId());
                if (subjectArticle == null) {
                    return;
                }
                url = "/rise/static/message/subject/reply?submitId=" + subjectArticle.getId();
            }
            sendMessage(message, toUser, SYSTEM_MESSAGE, url);
        });
    }

    private String getLikeMessage(VoteMessage voteMessage, Profile profile) {
        String message = "";
        if (voteMessage.getCount() == 1) {
            if (voteMessage.getType() == 2) {
                message = profile.getNickname() + "很喜欢你的应用练习作业，并给你点了赞";
            } else if (voteMessage.getType() == 3) {
                message = profile.getNickname() + "很喜欢你的小课分享作业，并给你点了赞";
            } else if (voteMessage.getType() == 4) {
                message = profile.getNickname() + "很认可你的回答，并给你点了赞";
            }
        } else {
            if (voteMessage.getType() == 2) {
                message = profile.getNickname() + "等" + voteMessage.getCount() + "人很喜欢你的应用练习作业，并给你点了赞";
            } else if (voteMessage.getType() == 3) {
                message = profile.getNickname() + "等" + voteMessage.getCount() + "人很喜欢你的小课分享作业，并给你点了赞";
            } else if (voteMessage.getType() == 4) {
                message = profile.getNickname() + "等" + voteMessage.getCount() + "人很认可你的回答，并给你点了赞";
            }
        }
        return message;
    }

    @Setter
    @Getter
    @ToString
    class VoteMessage<T> {
        private int referenceId;
        private int type;
        private T lastVote;
        private int count = 1;

        public VoteMessage(int referenceId, int type) {
            this.referenceId = referenceId;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VoteMessage)) return false;

            VoteMessage that = (VoteMessage) o;

            return referenceId == that.referenceId && type == that.type;

        }

        @Override
        public int hashCode() {
            int result = referenceId;
            result = 31 * result + type;
            return result;
        }

        public void increment() {
            this.count++;
        }
    }

    public void sendForumLikeMessage(List<AnswerApproval> answerApprovals) {
        List<VoteMessage<AnswerApproval>> voteMessageList = Lists.newArrayList();

        //自己给自己点赞不提醒
        answerApprovals.stream().filter(h1 -> !h1.getProfileId().equals(h1.getAnswerProfileId()))
                .forEach(answerApproval -> {
                    VoteMessage<AnswerApproval> voteMessage = new VoteMessage<>(answerApproval.getAnswerId(), 4);

                    //如果已经有了记录,点赞数+1
                    if (voteMessageList.contains(voteMessage)) {
                        voteMessageList.forEach(voteMessageInList -> {
                            if (voteMessageInList.equals(voteMessage)) {
                                voteMessageInList.increment();
                            }
                        });
                    } else {
                        //如果已经没有记录,添加记录
                        voteMessageList.add(voteMessage);
                    }
                    voteMessage.setLastVote(answerApproval);
                });

        //发送消息
        voteMessageList.forEach(voteMessage -> {
            AnswerApproval answerApproval = voteMessage.getLastVote();
            Integer profileId = answerApproval.getProfileId();
            Profile profile = profileDao.load(Profile.class, profileId);
            //没查到点赞人,不发消息
            if (profile == null) {
                logger.error("{} is not existed", profileId);
                return;
            }
            String message = getLikeMessage(voteMessage, profile);
            if (StringUtils.isEmpty(message)) {
                logger.error("{} is not supported", voteMessage);
                return;
            }
            String toUser = Objects.toString(answerApproval.getAnswerProfileId());
            String url = "/rise/static/message/question/answer?questionId=" + answerApproval.getQuestionId()
                    + "&answerId=" + answerApproval.getAnswerId();
            sendMessage(message, toUser, SYSTEM_MESSAGE, url);
        });
    }
}
