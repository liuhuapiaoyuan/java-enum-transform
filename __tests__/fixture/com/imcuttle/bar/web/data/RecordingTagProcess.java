/**
 * @(#)RecordingTaskRoleEnum.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

/**
 * @author xiechao01
 */
public enum RecordingTagProcess {

    ALL(1, "全部"),
    NOT_FINISHED(2, "未完成"),
    FINISHED(3, "已完成")
    ;

    private int value;

    private String name;

    RecordingTagProcess(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<RecordingTagProcess> findByInt(int value) {
        for (RecordingTagProcess item : RecordingTagProcess.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RecordingTagProcess> findByString(String name) {
        for (RecordingTagProcess item : RecordingTagProcess.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RecordingTagProcess findNullableByString(String name) {
        for (RecordingTagProcess item : RecordingTagProcess.values()) {
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
