/**
 * @(#)RecordingQuestionSource.java, 4月 12, 2021.
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
public enum RecordingApplicationQuestionSource {

    EXTERNAL(1, "外部题源"),
    ORIGINAL(2, "辅导自编");

    private int value;

    private String name;

    RecordingApplicationQuestionSource(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<RecordingApplicationQuestionSource> findByInt(int value) {
        for (RecordingApplicationQuestionSource item : RecordingApplicationQuestionSource.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RecordingApplicationQuestionSource> findByString(String name) {
        for (RecordingApplicationQuestionSource item : RecordingApplicationQuestionSource.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RecordingApplicationQuestionSource findNullableByString(String name) {
        for (RecordingApplicationQuestionSource item : RecordingApplicationQuestionSource.values()) {
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

    public static boolean isValidQuestionSource(int value) {
        return findByInt(value).isPresent();
    }
}
