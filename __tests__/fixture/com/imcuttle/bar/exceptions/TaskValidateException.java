/**
 * @(#)EsQueryException.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.exceptions;

import com.fenbi.commons2.rest.exception.BadRequestException;

/**
 * @author linbonan
 */
public class TaskValidateException extends BadRequestException {

    public TaskValidateException() {
        super();
    }

    public TaskValidateException(String message) {
        super(message);
    }
}
