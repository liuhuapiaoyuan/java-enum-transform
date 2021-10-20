/**
 * @(#)RecordingQuestionOrigin.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.foo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

/**
 * @author linbonan
 */
public enum RecordingQuestionOrigin {

    RECORDED(1,"新录入"),
    RELEASED(2,"已有题目");

    private int value;

    private String name;

    RecordingQuestionOrigin(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<RecordingQuestionOrigin> findByInt(int value) {
        for (RecordingQuestionOrigin item : RecordingQuestionOrigin.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RecordingQuestionOrigin> findByString(String name) {
        for (RecordingQuestionOrigin item : RecordingQuestionOrigin.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RecordingQuestionOrigin findNullableByString(String name) {
        for (RecordingQuestionOrigin item : RecordingQuestionOrigin.values()) {
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
