/**
 * @(#)RecordingTaskUpdatedEventProducer.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.message.producer;

import com.fenbi.common.alimq.constant.AlimqCluster;
import com.fenbi.common.alimq.producer.template.NormalAliMqProduceQueue;
import com.fenbi.common.alimq.transformer.JsonTransformer;
import com.fenbi.common.alimq.transformer.ObjectTransformer;
import com.imcuttle.bar.message.event.RecordingApplicationCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author chenyibo
 */
@Service
public class RecordingApplicationCreatedEventProducer extends NormalAliMqProduceQueue<RecordingApplicationCreatedEvent> {

    @Value("${aliMq.producer.topic.recording.name}")
    private String topic;

    @Value("${aliMq.cluster}")
    private String aliMqCluster;

    @Override
    protected ObjectTransformer<RecordingApplicationCreatedEvent> initObjectTransformer() {
        return new JsonTransformer<>(RecordingApplicationCreatedEvent.class);
    }

    @Override
    protected String initTopic() {
        return topic;
    }

    @Override
    protected AlimqCluster initAliMqCluster() {
        return AlimqCluster.findByString(aliMqCluster);
    }

    public void publish(long applicationId) {
        RecordingApplicationCreatedEvent event = new RecordingApplicationCreatedEvent(applicationId);
        super.publish(event, RecordingApplicationCreatedEvent.TAG_RECORDING_APPLICATION_CREATED);
    }
}
