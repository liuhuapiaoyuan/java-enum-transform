/**
 * @(#)FeedbackTarget.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.foo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imcuttle.foo.thrift.TutorArmoryConstants;

import java.util.Optional;

/**
 * @author linbonan
 */
public enum RecordingMode {

    EXAM_PAPER(TutorArmoryConstants.EXAM_PAPER, "套卷"),
    SINGLE_QUESTION(TutorArmoryConstants.SINGLE_QUESTION, "散题");

    private int value;

    private String name;

    RecordingMode(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<RecordingMode> findByInt(int value) {
        for (RecordingMode item : RecordingMode.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RecordingMode> findByString(String name) {
        for (RecordingMode item : RecordingMode.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RecordingMode findNullableByString(String name) {
        for (RecordingMode item : RecordingMode.values()) {
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
