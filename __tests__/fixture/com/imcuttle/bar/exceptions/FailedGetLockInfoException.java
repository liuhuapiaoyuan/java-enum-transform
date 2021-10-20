/**
 * @(#)FailedGetLockKeyException.java, 5月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.exceptions;

import com.fenbi.commons2.rest.exception.BadRequestException;

/**
 * @author zhangzhikuan
 */
public class FailedGetLockInfoException extends BadRequestException {

    public FailedGetLockInfoException() {
        super();
    }

    public FailedGetLockInfoException(String message) {
        super(message);
    }
}
