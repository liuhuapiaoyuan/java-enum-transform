/**
 * @(#)UserService.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.bar.data.UserBO;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author duanou
 */
public interface UserService {

    Optional<UserBO> getUserByUserId(int userId);

    Map<Integer, String> getUserNames(Collection<Integer> userIds);

    Map<Integer, UserInfo> getUserInfo(Collection<Integer> userIds);
}
