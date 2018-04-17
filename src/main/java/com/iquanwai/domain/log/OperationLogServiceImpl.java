package com.iquanwai.domain.log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.ClassMemberDao;
import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.dao.RiseClassMemberDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.dao.UserRoleDao;
import com.iquanwai.domain.dao.member.MemberTypeDao;
import com.iquanwai.domain.po.ClassMember;
import com.iquanwai.domain.po.MemberType;
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

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    @Autowired
    private MemberTypeDao memberTypeDao;
    @Autowired
    private ClassMemberDao classMemberDao;


    private Map<Integer, String> classNameMap = Maps.newHashMap();
    private Map<Integer, String> groupIdMap = Maps.newHashMap();


    @PostConstruct
    public void init() {
        memberTypeDao.loadAll(MemberType.class).forEach(item -> {
            classNameMap.put(item.getId(), "className:" + item.getId());
            groupIdMap.put(item.getId(), "groupId:" + item.getId());
        });
    }

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

                List<RiseMember> riseMemberList = riseMemberDao.loadAllValidRiseMembers(profileId).stream().filter(item -> item.getMemberTypeId() != RiseMember.COURSE).collect(Collectors.toList());
                if (!riseMemberList.isEmpty()) {
                    properties.put("roleNames", riseMemberList
                            .stream()
                            .map(RiseMember::getMemberTypeId)
                            .map(Object::toString)
                            .distinct()
                            .collect(Collectors.toList()));
                } else {
                    properties.put("roleNames", Lists.newArrayList("0"));
                }

                RiseClassMember riseClassMember = riseClassMemberDao.loadActiveRiseClassMember(profileId);
                if (riseClassMember == null) {
                    riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(profileId);
                }

                if (riseClassMember != null) {
                    if (riseClassMember.getClassName() != null) {
                        properties.put("className", riseClassMember.getClassName());
                    }
                    if (riseClassMember.getGroupId() != null) {
                        properties.put("groupId", riseClassMember.getGroupId());
                    }
                }

                List<ClassMember> classMembers = classMemberDao.loadActiveByProfileId(profileId);
                if (classMembers.isEmpty()) {
                    ClassMember exist = classMemberDao.loadLatestByProfileId(profileId);
                    if (exist != null) {
                        classMembers = Lists.newArrayList(exist);
                    }
                }
                if (!classMembers.isEmpty()) {
                    classMembers.forEach(item -> {
                        if (item.getClassName() != null) {
                            properties.put(classNameMap.get(item.getMemberTypeId()), item.getClassName());
                        }
                        if (item.getGroupId() != null) {
                            properties.put(groupIdMap.get(item.getMemberTypeId()), item.getGroupId());
                        }
                    });
                }

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
