/**
 * @(#)RecordingAuditResourceVO.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lukebj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingAuditResource {

    private long id;

    private long targetId;

    /**
     * @see com.imcuttle.enums.FeedbackTargetType
     */
    private int targetType;

    private long examPaperId;

    private int questionOrigin;

    private int stage;
}
