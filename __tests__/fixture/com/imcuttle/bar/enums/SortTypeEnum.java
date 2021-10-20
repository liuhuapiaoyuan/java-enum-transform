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
public enum SortTypeEnum {

    SUBMIT_TIME_ASC(1, "submitTimeAsc"), // 申请时间
    SUBMIT_TIME_DESC(2, "submitTimeDesc"),
    PUBLISH_TIME_ASC(3, "publishTimeAsc"),// 发布时间
    PUBLISH_TIME_DESC(4, "publishTimeDesc"),
    TASK_SUBMIT_TIMES_ASC(5, "taskSubmitTimesAsc"),// 审核轮次
    TASK_SUBMIT_TIMES_DESC(6, "taskSubmitTimesDesc"),
    RECORDED_QUESTION_COUNT_ASC(7, "recordedQuestionCountAsc"),// 录题数
    RECORDED_QUESTION_COUNT_DESC(8, "recordedQuestionCountDesc"),
    COMPLETE_DURATION_ASC(9, "completeDurationInMillisecondsAsc"),// 完成总耗时
    COMPLETE_DURATION_DESC(10, "completeDurationInMillisecondsDesc");

    private int value;

    private String name;

    SortTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<SortTypeEnum> findByInt(int value) {
        for (SortTypeEnum item : SortTypeEnum.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<SortTypeEnum> findByString(String name) {
        for (SortTypeEnum item : SortTypeEnum.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static SortTypeEnum findNullableByString(String name) {
        for (SortTypeEnum item : SortTypeEnum.values()) {
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
