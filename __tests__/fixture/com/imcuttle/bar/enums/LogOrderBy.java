/**
 * @(#)LogOrderBy.java, 5月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

/**
 * @author linbonan
 */
public enum LogOrderBy {

    CREATED_TIME_DESC("createdTimeDesc"),

    CREATED_TIME_ASC("createdTimeAsc");

    private String name;

    LogOrderBy(String name) {
        this.name = name;
    }

    @JsonCreator
    public static Optional<LogOrderBy> findByString(String name) {
        for (LogOrderBy item : LogOrderBy.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonValue
    @Override
    public String toString() {
        return this.name;
    }
}
