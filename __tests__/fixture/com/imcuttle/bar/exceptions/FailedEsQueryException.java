/**
 * @(#)EsQueryException.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.exceptions;

/**
 * @author linbonan
 */
public class FailedEsQueryException extends RuntimeException {

    public FailedEsQueryException() {
        super();
    }

    public FailedEsQueryException(String message) {
        super(message);
    }
}
