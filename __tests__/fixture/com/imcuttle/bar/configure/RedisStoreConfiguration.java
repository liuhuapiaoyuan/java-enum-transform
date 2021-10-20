/**
 * @(#)RedisStoreConfiguration.java, 5月 06, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.configure;

import com.fenbi.common.jedis.JedisClient;
import com.fenbi.common.jedis.configure.BaseJedisConfiguration;
import com.fenbi.common.jedis.lock.DistributeLocker;
import com.fenbi.common.jedis.lock.JedisDistributeLocker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangzhikuan
 */
@Configuration
@ConfigurationProperties(prefix = "redis.store")
public class RedisStoreConfiguration extends BaseJedisConfiguration {

    @Bean(name = "storeJedisClient")
    public JedisClient storeJedisClient() {
        return super.createJedisClient();
    }

    @Bean(name = "redisDistributeLocker")
    public DistributeLocker redisDistributeLocker(@Qualifier("storeJedisClient") JedisClient redisClient) {
        JedisDistributeLocker redisDistributeLocker = new JedisDistributeLocker(redisClient);
        redisDistributeLocker.setLockExpireTime(JedisDistributeLocker.DEFAULT_LOCK_EXPIRE_TIME);
        redisDistributeLocker.setLockKeyPrefix(JedisDistributeLocker.DEFAULT_LOCK_KEY_PREFIX);
        return redisDistributeLocker;
    }
}
