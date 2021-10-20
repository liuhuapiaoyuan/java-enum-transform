package com.imcuttle.bar.configure;

import com.imcuttle.bar.rpc.RpcHandler;
import com.imcuttle.thrift.TutorArmoryThrift;
import org.apache.thrift.TProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linbonan
 */
@Configuration
@ConditionalOnProperty(name = "rpcServer.enabled", matchIfMissing = true)
public class RpcServerConfiguration {

    @Bean(name = "thriftProcessor")
    @SuppressWarnings("unchecked")
    public TProcessor processor(RpcHandler handler) {
        return new TutorArmoryThrift.Processor(handler);
    }
}
