/**
 * @(#)LogIdType.java, 5月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants;

import java.util.Optional;

/**
 * @author linbonan
 */
public enum LogIdType {

    RECORDING_APPLICATION_ID(TutorCmsLogConstants.RECORDING_APPLICATION_ID, "recording_application_id"),
    QUESTION_ID(TutorCmsLogConstants.QUESTION_ID, "question_id"),
    EXAM_PAPER_ID(TutorCmsLogConstants.EXAM_PAPER, "exam_paper");

    private int value;

    private String name;

    LogIdType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static Optional<LogIdType> findByInt(int value) {
        for (LogIdType item : LogIdType.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<LogIdType> findByString(String name) {
        for (LogIdType item : LogIdType.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static LogIdType findNullableByString(String name) {
        for (LogIdType item : LogIdType.values()) {
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
