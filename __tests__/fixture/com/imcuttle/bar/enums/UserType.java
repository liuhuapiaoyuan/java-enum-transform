/**
 * @(#)UserType.java, 七月 06, 2016.
 * <p>
 * Copyright 2016 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fenbi.tutor.crmlog.thrift.TutorCrmLogConstants;

/**
 * @author zhangpeng
 */
public enum UserType {

    UNKNOWN(TutorCrmLogConstants.UNKNOWN, "unknown"),

    TEACHER(TutorCrmLogConstants.TEACHER, "teacher"),

    STUDENT(TutorCrmLogConstants.STUDENT, "student"),

    ADMIN(TutorCrmLogConstants.ADMIN, "admin"),

    ASSISTANT(TutorCrmLogConstants.ASSISTANT, "assistant"),

    LDAP(TutorCrmLogConstants.LDAP, "ldap"),

    MENTOR(TutorCrmLogConstants.MENTOR, "mentor");

    private int value;

    private String name;

    UserType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static UserType findByInt(int value) throws IllegalArgumentException {
        for (UserType item : UserType.values()) {
            if (item.value == value) {
                return item;
            }
        }

        throw new IllegalArgumentException("Invalid UserType value: " + value);
    }

    @JsonCreator
    public static UserType findByString(String name) throws IllegalArgumentException {
        for (UserType item : UserType.values()) {
            if (item.name.equals(name)) {
                return item;
            }
        }

        throw new IllegalArgumentException("Invalid UserType name: " + name);
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
