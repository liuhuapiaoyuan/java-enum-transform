/**
 * @(#)AbstractLogDiffServiceImpl.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.bar.data.Change;
import com.imcuttle.bar.enums.ChangeTypeEnum;
import com.imcuttle.bar.service.LogDiffService;
import com.imcuttle.bar.service.diff.PropertyFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenkangbj
 */
@Slf4j
public abstract class AbstractLogDiffService<T> implements LogDiffService<T> {

    @Override
    public List<Change> diff(T oldObj, T newObj) {
        return diff(oldObj, newObj, getAllPropertyNames());
    }

    @Override
    public List<Change> diff(T oldObj, T newObj, Collection<String> includePropertyNames) {
        List<Change> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(includePropertyNames)) {
            return result;
        }
        for (String name : includePropertyNames) {
            Change change = diffProperty(oldObj, newObj, name);
            if (change.getChangeType() != ChangeTypeEnum.UNCHANGED) {
                result.add(change);
            }
        }
        return result;
    }

    @Override
    public Change diffProperty(T oldObj, T newObj, String name) {
        Change change = new Change();
        change.setBeanPropertyName(name);
        change.setFieldName(getFieldName(name));
        change.setChangeType(ChangeTypeEnum.UNCHANGED);

        try {
            PropertyDescriptor property = BeanUtils.getPropertyDescriptor(getBeanClass(), name);
            Method getter = property.getReadMethod();
            Object oldVal = getter.invoke(oldObj);
            Object newVal = getter.invoke(newObj);
            PropertyFormatter formatter = getFormatterForProperty(name);
            change.setOldValue(Optional.ofNullable(oldVal).map(formatter::format).orElse(""));
            change.setNewValue(Optional.ofNullable(newVal).map(formatter::format).orElse(""));

            if (!Objects.equals(oldVal, newVal)) {
                change.setChangeType(oldVal == null ? ChangeTypeEnum.ADD : ChangeTypeEnum.UPDATE);
            }
        } catch (Exception e) {
            log.error("Exception when diff, skip! propertyName = {}, property = {}, old = {}, new = {}", name, oldObj, newObj, e);
        }

        return change;
    }

    protected abstract Class<T> getBeanClass();

    protected abstract String getFieldName(String propertyName);

    protected abstract PropertyFormatter getFormatterForProperty(String propertyName);
}
