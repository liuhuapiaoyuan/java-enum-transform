/**
 * @(#)MapUtil.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.util;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.apache.commons.collections4.MapUtils.isEmpty;

/**
 * @author chenkangbj
 */
public class MapUtil {

    /**
     * 转换Map的value。如果converter对key的计算结果为null，返回的结果中将不包含该key。
     *
     * @param originMap
     * @param converter
     * @param <K>
     * @param <A>
     * @param <B>
     * @return
     */
    public static <K, A, B> Map<K, B> convertValue(Map<K, A> originMap, Function<A, B> converter) {
        if (isEmpty(originMap)) {
            return new HashMap<>();
        }
        Map<K, B> result = Maps.newHashMap();
        for (Map.Entry<K, A> entry : originMap.entrySet()) {
            K key = entry.getKey();
            A oldVal = entry.getValue();
            if (oldVal == null) {
                continue;
            }

            B newVal = converter.apply(oldVal);
            if (newVal == null) {
                continue;
            }
            result.put(key, newVal);
        }
        return result;
    }

    public static <K, V> Map<K, V> getOrEmpty(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }
}
