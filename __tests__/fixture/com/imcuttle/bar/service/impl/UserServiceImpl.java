/**
 * @(#)UserServiceImpl.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.commons.cache.ICache;
import com.fenbi.commons.cache.impl.ExpirableLruLocalCache;
import com.fenbi.commons.cache.util.CacheUtils;
import com.imcuttle.bar.data.UserBO;
import com.imcuttle.bar.enums.UserStatusEnum;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.util.UserUtil;
import com.fenbi.tutor.crmaccount.client.proxy.TutorCrmAccountProxy;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import com.yuanfudao.tutor.truser.client.proxy.TutorTrUserProxy;
import com.yuanfudao.tutor.truser.enums.TrRoleType;
import com.yuanfudao.tutor.truser.thrift.UserTrRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author duanou
 */
@Service
public class UserServiceImpl implements UserService {

    private static final int CACHE_SIZE = 200;
    private static final int CACHE_TTL_IN_SECONDS = (int) TimeUnit.HOURS.toSeconds(1L);
    private final ICache<Integer, UserInfo> userIdToInfoCache = new ExpirableLruLocalCache<>(CACHE_SIZE);

    @Autowired
    private TutorCrmAccountProxy tutorCrmAccountProxy;

    @Autowired
    private TutorTrUserProxy tutorTrUserProxy;

    @Override
    public Optional<UserBO> getUserByUserId(int userId) {
        if (userId <= 0) {
            return Optional.empty();
        }

        Optional<UserTrRole> userTrRoleOp = tutorTrUserProxy
                .cacheGetByUserIdAndTrRoleType(userId, TrRoleType.ARMORY_USER.toInt());
        if (!userTrRoleOp.isPresent()) {
            return Optional.empty();
        }
        UserTrRole userTrRole = userTrRoleOp.get();
        UserBO result = new UserBO();
        result.setUserId(userTrRole.getUserId());
        result.setStatus(findStatus(userTrRole.getStatus()));
        return Optional.of(result);
    }

    @Override
    public Map<Integer, String> getUserNames(Collection<Integer> userIds) {
        return CacheUtils.cacheGet(userIdToInfoCache, userIds, tutorCrmAccountProxy::getUserInfosByIds, CACHE_TTL_IN_SECONDS)
                .values()
                .stream()
                .collect(Collectors.toMap(UserInfo::getId, UserUtil::formatToName));
    }

    @Override
    public Map<Integer, UserInfo> getUserInfo(Collection<Integer> userIds) {
        return CacheUtils.cacheGet(userIdToInfoCache, userIds, tutorCrmAccountProxy::getUserInfosByIds, CACHE_TTL_IN_SECONDS);
    }

    private UserStatusEnum findStatus(int status) {
        return Optional.ofNullable(UserStatusEnum.findByInt(status)).orElse(UserStatusEnum.INVALID);
    }
}
