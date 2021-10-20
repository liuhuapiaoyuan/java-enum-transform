/**
 * @(#)Change.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import com.imcuttle.bar.enums.ChangeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenkangbj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Change {

    private ChangeTypeEnum changeType;

    private String beanPropertyName;

    private String fieldName;

    private String oldValue;

    private String newValue;

    @Override
    public String toString() {
        if (changeType == null) {
            return "";
        }

        switch (changeType) {
            case ADD:
                return String.format("%s={%s}", fieldName, newValue);
            case UPDATE:
                return String.format("%s:{%s}=>{%s}", fieldName, oldValue, newValue);
            case UNCHANGED:
            default:
                return "";
        }
    }
}
