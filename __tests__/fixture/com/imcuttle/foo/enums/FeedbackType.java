/**
 * @(#)FeedbackType.java, 4月 12, 2021.
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
public enum FeedbackType {

    APPLICATION_NEED_REVISED(1, "录题申请需修改", 1),
    APPLICATION_REJECTED(2, "录题申请不通过", 1),
    APPLICATION_APPROVED(3, "录题申请通过", 1),
    APPLICATION_CANCELED(4, "录题申请被取消", 1),

    QUESTION_APPROVED(5, "题目审核通过", 2),
    QUESTION_AUDIT_FAILED(6, "题目审核不通过", 2),
    QUESTION_NEED_REVISED(7, "题目纠错", 2),

    PAPER_AUDIT_APPROVED(8, "试卷审核通过", 3),
    PAPER_AUDIT_FAILED(9, "试卷审核不通过", 3),

    TASK_AUDIT_FAILED(10, "录题任务审核不通过", 4);

    private int value;

    private String name;

    private int requiredTargetType; // see FeedbackTargetType

    FeedbackType(int value, String name, int requiredTargetType) {
        this.value = value;
        this.name = name;
        this.requiredTargetType = requiredTargetType;
    }

    public static Optional<FeedbackType> findByInt(int value) {
        for (FeedbackType item : FeedbackType.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<FeedbackType> findByString(String name) {
        for (FeedbackType item : FeedbackType.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static FeedbackType findNullableByString(String name) {
        for (FeedbackType item : FeedbackType.values()) {
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

    public int getRequiredTargetType() {
        return requiredTargetType;
    }
}
