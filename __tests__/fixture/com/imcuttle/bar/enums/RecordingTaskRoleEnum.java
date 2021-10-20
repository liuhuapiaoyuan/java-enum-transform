/**
 * @(#)RecordingTaskRoleEnum.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

/**
 * @author xiechao01
 */
public enum RecordingTaskRoleEnum {

    RECORDING_TASK_RECORDER(1, "recorder"),
    RECORDING_TASK_AUDITOR(2, "auditor"),
    ;

    private int value;

    private String name;

    RecordingTaskRoleEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<RecordingTaskRoleEnum> findByInt(int value) {
        for (RecordingTaskRoleEnum item : RecordingTaskRoleEnum.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RecordingTaskRoleEnum> findByString(String name) {
        for (RecordingTaskRoleEnum item : RecordingTaskRoleEnum.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RecordingTaskRoleEnum findNullableByString(String name) {
        for (RecordingTaskRoleEnum item : RecordingTaskRoleEnum.values()) {
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
