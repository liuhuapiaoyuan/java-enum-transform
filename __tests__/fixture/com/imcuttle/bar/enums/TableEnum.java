/**
 * @(#)TableEnum.java, 4月 22, 2021.
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
public enum TableEnum {

    RECORDING_APPLICATION("application", "录题申请"),
    RECORDING_TASK("task", "录题任务");

    private String key;

    private String name;

    TableEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public static Optional<TableEnum> findByKey(String key) {
        for (TableEnum item : TableEnum.values()) {
            if (item.key.equals(key)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<TableEnum> findByString(String name) {
        for (TableEnum item : TableEnum.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static TableEnum findNullableByString(String name) {
        for (TableEnum item : TableEnum.values()) {
            if (item.name.equals(name)) {
                return item;
            }
        }

        return null;
    }

    @JsonValue
    public String toString() {
        return this.name;
    }

    public String toKey() {
        return this.key;
    }
}
