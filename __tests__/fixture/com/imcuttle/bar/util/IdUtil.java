/**
 * @(#)IdUtil.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.util;

/**
 * @author chenkangbj
 */
public class IdUtil {

    private IdUtil() {}

    public static boolean isDefaultId(long id) {
        return id == 0L;
    }
}
