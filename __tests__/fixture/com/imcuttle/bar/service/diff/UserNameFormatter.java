/**
 * @(#)UserNameFormatter.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.diff;

import com.imcuttle.bar.service.UserService;

import java.util.Collections;

/**
 * @author chenkangbj
 */
public class UserNameFormatter implements PropertyFormatter {

    private final UserService userService;

    public UserNameFormatter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String format(Object obj) {
        if (!(obj instanceof Number)) {
            return "";
        }

        int userId = ((Number) obj).intValue();
        return userService.getUserNames(Collections.singletonList(userId)).getOrDefault(userId, "" + userId);
    }
}
