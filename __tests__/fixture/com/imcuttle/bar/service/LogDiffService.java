/**
 * @(#)LogDiffService.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.bar.data.Change;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author chenkangbj
 */
public interface LogDiffService<T> {

    List<Change> diff(T oldObj, T newObj);

    List<Change> diff(T oldObj, T newObj, Collection<String> includePropertyNames);

    Change diffProperty(T oldObj, T newObj, String name);

    Set<String> getAllPropertyNames();
}
