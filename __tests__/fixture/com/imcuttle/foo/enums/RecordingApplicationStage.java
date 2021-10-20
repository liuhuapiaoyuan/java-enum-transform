/**
 * @(#)RecordingApplicationStage.java, 4月 12, 2021.
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
public enum RecordingApplicationStage {

    SUBMITTED(1, "待审核"),
    TO_BE_REVISED(2,"驳回待修改"),
    REJECTED(3,"审核不通过"),
    TO_BE_ASSIGNED(4,"待分配"),
    PROCESSING(5,"任务进行中"),
    PUBLISHED(6,"已发布"),
    CANCELED(7,"已取消");

    private int value;

    private String name;

    RecordingApplicationStage(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<RecordingApplicationStage> findByInt(int value) {
        for (RecordingApplicationStage item : RecordingApplicationStage.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RecordingApplicationStage> findByString(String name) {
        for (RecordingApplicationStage item : RecordingApplicationStage.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RecordingApplicationStage findNullableByString(String name) {
        for (RecordingApplicationStage item : RecordingApplicationStage.values()) {
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
