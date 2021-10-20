/**
 * @(#)SortTypeEnum.java, 4月 12, 2021.
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
public enum ChangeTypeEnum {

    UNCHANGED(1, "未变化"),
    ADD(2, "新增"),
    UPDATE(3, "变更")
    ;

    private int value;

    private String name;

    ChangeTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<ChangeTypeEnum> findByInt(int value) {
        for (ChangeTypeEnum item : ChangeTypeEnum.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<ChangeTypeEnum> findByString(String name) {
        for (ChangeTypeEnum item : ChangeTypeEnum.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static ChangeTypeEnum findNullableByString(String name) {
        for (ChangeTypeEnum item : ChangeTypeEnum.values()) {
            if (item.name.equals(name)) {
                return item;
            }
        }

        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.name;
    }

    public int toInt() {
        return this.value;
    }
}
