/**
 * @(#)UserBO.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import com.imcuttle.bar.enums.UserStatusEnum;
import lombok.Data;

/**
 * @author duanou
 */
@Data
public class UserBO {

    private long userId;
    private UserStatusEnum status;
}
