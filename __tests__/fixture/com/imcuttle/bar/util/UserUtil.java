/**
 * @(#)UserUtil.java, 4月 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.util;

import com.fenbi.commons.security.UserRole;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.ADMIN;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.ASSISTANT;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.LDAP;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.MENTOR;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.TEACHER;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.UNKNOWN;
import static com.fenbi.tutor.trfilter.TrUserRole.ARMORY_USER;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * @author linbonan
 */
public class UserUtil {

    private static final String USER_ROLE_TEACHER = "ROLE_TEACHER";
    private static final String USER_ROLE_MENTOR = "ROLE_MENTOR";
    private static final String USER_ROLE_ASSISTANT = "ROLE_ASSISTANT";
    private static final String USER_NAME_TEMPLATE = "%s(%s)";
    private static final String PLACE_HOLDER_FOR_MISSING_PARAM = "-";

    public static String formatToName(UserInfo userInfo) {
        if (Objects.isNull(userInfo)) {
            return PLACE_HOLDER_FOR_MISSING_PARAM;
        }

        String simpleName = StringUtils.isBlank(userInfo.getNickname()) ? String.valueOf(userInfo.getId()) : userInfo.getNickname();
        String phone = StringUtils.isBlank(userInfo.getPhone()) ? PLACE_HOLDER_FOR_MISSING_PARAM : userInfo.getPhone();
        return String.format(USER_NAME_TEMPLATE, simpleName, phone);
    }

    public static int getUserType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> authorities = new ArrayList<>();
        if (authentication != null && isNotEmpty(authentication.getAuthorities())) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                authorities.add(authority.getAuthority());
            }
        }

        // staff
        if (authorities.contains(UserRole.STAFF)) {
            return  LDAP;
        }

        // mentor
        if (authorities.contains(USER_ROLE_MENTOR)) {
            // mentor同时是老师，因此需要优先判断mentor身份
            return MENTOR;
        }

        // assistant
        if (authorities.contains(USER_ROLE_ASSISTANT)) {
            return ASSISTANT;
        }

        // teacher
        if (authorities.contains(USER_ROLE_TEACHER)) {
            return TEACHER;
        }

        // unknown(仅有录题平台用户的身份, 对于公司的atms体系来讲, 就是 unknown)
        if (authorities.contains(ARMORY_USER)) {
            return UNKNOWN;
        }

        // 不包含权限认证信息，一般是consumer或者定时任务，认为是ADMIN
        return ADMIN;
    }

    private UserUtil() {
    }
}
