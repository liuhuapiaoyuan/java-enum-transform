/**
 * @(#)RecordingTaskStage.java, 4月 12, 2021.
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
public enum RecordingTaskStage {

    /*
    * 录题中
    */
    RECORDING(1, "录题中"),
    //  待审题
    SUBMITTED(2, "待审核"),
    /*激活中*/
    TO_BE_REVISED(3,"驳回待修改"),
    AUDITED(4,"审核通过待标注"),
    PUBLISHED(5,"已发布"),
    CANCELED(6,"已取消");

    private int value;

    private String name;

    RecordingTaskStage(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<RecordingTaskStage> findByInt(int value) {
        for (RecordingTaskStage item : RecordingTaskStage.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RecordingTaskStage> findByString(String name) {
        for (RecordingTaskStage item : RecordingTaskStage.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RecordingTaskStage findNullableByString(String name) {
        for (RecordingTaskStage item : RecordingTaskStage.values()) {
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
