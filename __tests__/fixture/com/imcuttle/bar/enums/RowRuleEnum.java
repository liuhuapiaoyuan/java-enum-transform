/**
 * @(#)RowRuleEnum.java, 4月 22, 2021.
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
public enum RowRuleEnum {

    ALL_PHASE_SUBJECT("all", "所有学科学段"),
    PHASE_SUBJECT_OF_ROLE("same-phase-subject", "与角色相同的学科学段");

    private String key;

    private String name;

    RowRuleEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public static Optional<RowRuleEnum> findByKey(String key) {
        for (RowRuleEnum item : RowRuleEnum.values()) {
            if (item.key.equals(key)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<RowRuleEnum> findByString(String name) {
        for (RowRuleEnum item : RowRuleEnum.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static RowRuleEnum findNullableByString(String name) {
        for (RowRuleEnum item : RowRuleEnum.values()) {
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

    public String toKey() {
        return this.key;
    }
}
