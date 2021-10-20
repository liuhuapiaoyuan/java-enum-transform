/**
 * @(#)UserVO.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import com.imcuttle.bar.enums.UserStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author duanou
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class UserVO {

    private static final long INVALID_USER_ID = -1;

    private static final int INVALID_USER_STATUS = UserStatusEnum.INVALID.toInt();

    private long userId;

    private int status;

    public static UserVO newInvalidUser() {
        UserVO userVO = new UserVO();
        userVO.setUserId(INVALID_USER_ID);
        userVO.setStatus(INVALID_USER_STATUS);
        return userVO;
    }
}
