/**
 * @(#)UserInfoVO.java, 5月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import lombok.Data;

/**
 * @author linbonan
 */
@Data
public class UserInfoVO {

    private int id;

    private String nickname;

    public UserInfoVO(UserInfo userInfo) {
        this.id = userInfo.getId();
        this.nickname = userInfo.getNickname();
    }
}
