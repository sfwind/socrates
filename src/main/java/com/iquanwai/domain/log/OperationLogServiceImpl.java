package com.iquanwai.domain.log;

import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.dao.RiseClassMemberDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.dao.UserRoleDao;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.domain.po.RiseClassMember;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.UserRole;
import com.iquanwai.util.ThreadPool;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.support.Assert;

import java.util.Map;
import java.util.function.Supplier;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SensorsAnalytics sa;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private ProfileDao profileDao;

    @Override
    public void trace(Supplier<Integer> profileIdSupplier, String eventName, Supplier<Prop> supplier) {
        ThreadPool.execute(() -> {
            try {
                Integer profileId = profileIdSupplier.get();
                Prop prop = supplier.get();
                Map<String, Object> properties = prop.build();
                Assert.notNull(profileId, "用户id不能为null");
                Profile profile = profileDao.load(Profile.class, profileId);
                UserRole role = userRoleDao.getAssist(profileId);

                Integer roleName = 0;
                RiseMember validRiseMember = riseMemberDao.loadValidRiseMember(profileId);

                RiseClassMember riseClassMember = riseClassMemberDao.loadActiveRiseClassMember(profileId);
                if (riseClassMember == null) {
                    riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(profileId);
                }

                if (riseClassMember != null) {
                    properties.put("className", riseClassMember.getClassName());
                    properties.put("groupId", riseClassMember.getGroupId());
                }
                if (validRiseMember != null) {
                    roleName = validRiseMember.getMemberTypeId();
                }
                properties.put("roleName", roleName);
                properties.put("isAsst", role != null);
                properties.put("riseId", profile.getRiseId());

                sa.track(profile.getRiseId(), true, eventName, properties);
                //  上线前删掉
//                sa.flush();
            } catch (InvalidArgumentException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });
    }

    @Override
    public void trace(Integer profileId, String eventName) {
        this.trace(() -> profileId, eventName, OperationLogService::props);
    }

    @Override
    public void trace(Integer profileId, String eventName, Supplier<Prop> supplier) {
        this.trace(() -> profileId, eventName, supplier);
    }
}
