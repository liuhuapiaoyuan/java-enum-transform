/**
 * @(#)LockPrefixEnum.java, 5月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Optional;

/**
 * @author zhangzhikuan
 */
public enum LockPrefixEnum {

    PAPER_PREFIX(1,"paper"),
    QUESTION_PREFIX(2, "question"),
    RECORDING_TASK(3, "recordingTask");

    private int key;

    private String name;

    LockPrefixEnum(int key, String name) {
        this.key = key;
        this.name = name;
    }

    public static Optional<LockPrefixEnum> findByKey(int key) {
        for (LockPrefixEnum prefix : LockPrefixEnum.values()) {
            if (prefix.key == key) {
                return Optional.of(prefix);
            }
        }

        return Optional.empty();
    }

    public static Optional<LockPrefixEnum> findByString(String name) {
        for (LockPrefixEnum prefix : LockPrefixEnum.values()) {
            if (prefix.name.equals(name)) {
                return Optional.of(prefix);
            }
        }
        return Optional.empty();
    }

    @JsonValue
    @Override
    public String toString() {
        return this.name;
    }

    public int toInt() {
        return this.key;
    }
}
