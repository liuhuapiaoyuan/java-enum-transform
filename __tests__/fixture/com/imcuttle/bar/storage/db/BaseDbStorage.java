/**
 * @(#)BaseDbStorage.java, Dec 28, 2015.
 * <p/>
 * Copyright 2015 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db;

import com.fenbi.common.db.DbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author linbonan
 */
public class BaseDbStorage {

    @Autowired
    @Qualifier("dbClient")
    protected DbClient dbClient;
}
