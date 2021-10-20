/**
 * @(#)CmsLogGeneratedEventAliMqConsumer.java, Nov 13, 2020.
 * <p>
 * Copyright 2020 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.message.consumer;

import com.fenbi.common.alimq.constant.AlimqCluster;
import com.fenbi.common.alimq.consumer.impl.AbstractSingleNormalConsumer;
import com.fenbi.common.alimq.consumer.impl.MessageContext;
import com.fenbi.common.alimq.transformer.JsonTransformer;
import com.fenbi.common.alimq.transformer.ObjectTransformer;
import com.imcuttle.bar.message.event.CmsLogGeneratedEvent;
import com.fenbi.tutor.cmslog.client.proxy.TutorCmsLogProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.imcuttle.bar.util.CollectionUtil.getOrEmpty;

/**
 * @author chenkangbj
 */
@Slf4j
@Service
public class CmsLogGeneratedEventAliMqConsumer extends AbstractSingleNormalConsumer<CmsLogGeneratedEvent> {

    @Value("${aliMq.consumer.topic.cmsLogGenerated.name}")
    private String topic;

    @Value("${aliMq.cluster}")
    private String aliMqCluster;

    @Value("${aliMq.consumer.topic.cmsLogGenerated.consumerId}")
    private String consumerId;

    @Autowired
    private TutorCmsLogProxy tutorCmsLogProxy;

    @Override
    protected ObjectTransformer<CmsLogGeneratedEvent> createObjectTransformer() {
        return new JsonTransformer<>(CmsLogGeneratedEvent.class);
    }

    @Override
    protected String tags() {
        return CmsLogGeneratedEvent.TAGS;
    }

    @Override
    protected String topic() {
        return topic;
    }

    @Override
    protected String consumerId() {
        return consumerId;
    }

    @Override
    protected AlimqCluster aliMqCluster() {
        return AlimqCluster.findByString(aliMqCluster);
    }

    @Override
    protected boolean doProcess(CmsLogGeneratedEvent event, MessageContext messageContext) {
        log.info("CmsLogGeneratedEvent consume start, event = {}, context = {}", event, messageContext);
        getOrEmpty(event.getCmsLogList()).forEach(cmsLog -> tutorCmsLogProxy.createCmsLog(cmsLog));
        log.info("CmsLogGeneratedEvent consume finish, event = {}, context = {}", event, messageContext);
        return true;
    }
}
