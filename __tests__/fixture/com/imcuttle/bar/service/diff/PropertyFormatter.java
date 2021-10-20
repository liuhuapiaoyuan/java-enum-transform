/**
 * @(#)PropertyDiffHandler.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.diff;

/**
 * @author chenkangbj
 */
@FunctionalInterface
public interface PropertyFormatter {

    String format(Object obj);
}
