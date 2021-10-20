/**
 * @(#)CollectionUtil.java, 四月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.util;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author chenkangbj
 */
public class CollectionUtil {

    public static <T> List<T> getOrEmpty(List<T> list) {
        return list == null ? Lists.newArrayList() : list;
    }
}
