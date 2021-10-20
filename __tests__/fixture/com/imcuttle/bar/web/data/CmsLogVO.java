/**
 * @(#)CmsLogVO.java, 七月 06, 2016.
 * <p>
 * Copyright 2016 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import com.imcuttle.bar.enums.LogIdType;
import com.imcuttle.bar.enums.UserType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zhangpeng
 */
@Data
public class CmsLogVO {

    private long id;

    private LogIdType idType;

    private UserType userType;

    private String ldap;

    @ApiModelProperty("当操作人为非 ldap 时，有值，仅暴露 userId 和 nickName")
    private UserInfoVO userInfo;

    private String content;

    private long createdTime;
}
