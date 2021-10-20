/**
 * @(#)UserStatusEnum.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author duanou
 */
public enum UserStatusEnum {
    VALID(1, "有效"),
    INVALID(-1, "失效")
    ;

    private int value;

    private String name;

    UserStatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static UserStatusEnum findByInt(int value) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }

        return null;
    }

    @JsonCreator
    public static UserStatusEnum findByString(String name) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
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

    public int toInt() {
        return this.value;
    }
}
