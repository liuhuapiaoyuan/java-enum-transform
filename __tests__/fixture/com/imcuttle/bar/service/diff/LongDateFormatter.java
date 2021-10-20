/**
 * @(#)LongDateFormatter.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.diff;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author chenkangbj
 */
public class LongDateFormatter implements PropertyFormatter {

    @Override
    public String format(Object obj) {
        if (!(obj instanceof Long)) {
            return "";
        }

        long time = (long) obj;
        return time <= 0 ? "" : new SimpleDateFormat("yyyy.MM.dd").format(new Date(time));
    }
}
