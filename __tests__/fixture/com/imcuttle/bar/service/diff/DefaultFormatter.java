/**
 * @(#)DefaultFormatter.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.diff;

/**
 * @author chenkangbj
 */
public class DefaultFormatter implements PropertyFormatter {

    @Override
    public String format(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
