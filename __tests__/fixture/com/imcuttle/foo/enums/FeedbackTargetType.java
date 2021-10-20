/**
 * @(#)FeedbackTarget.java, 4月 12, 2021.
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
public enum FeedbackTargetType {

    APPLICATION(1, "录题申请"),
    QUESTION(2, "题目"),
    EXAM_PAPER(3, "试卷"),
    RECORDING_TASK(4, "录入任务");

    private int value;

    private String name;

    FeedbackTargetType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<FeedbackTargetType> findByInt(int value) {
        for (FeedbackTargetType item : FeedbackTargetType.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<FeedbackTargetType> findByString(String name) {
        for (FeedbackTargetType item : FeedbackTargetType.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static FeedbackTargetType findNullableByString(String name) {
        for (FeedbackTargetType item : FeedbackTargetType.values()) {
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
