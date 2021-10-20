/**
 * @(#)SubjectPhasePair.java, 4月 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linbonan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectPhasePair {

    private int subjectId;
    private int phaseId;
}
