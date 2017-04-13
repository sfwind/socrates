package com.iquanwai.domain;

import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/4/13.
 */
@Service
public class CustomerService {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

    public void checkMemberExpired(){
        List<RiseMember> riseMembers = riseMemberDao.loadAll(RiseMember.class);
        for (RiseMember riseMember : riseMembers) {
            if (!riseMember.getExpireDate().after(new Date())) {
                try {
                    logger.info("user:{} expired ad {}", riseMember.getOpenId(), DateUtils.parseDateTimeToString(riseMember.getExpireDate()));
                    profileDao.riseMemberExpired(riseMember.getOpenId());
                    riseMemberDao.riseMemberExpired(riseMember);
                } catch (Exception e){
                    logger.error("expired: {} error", riseMember.getOpenId());
                }
            }
        }
    }
}
