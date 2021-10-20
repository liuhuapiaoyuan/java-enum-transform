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
import com.fenbi.common.db.util.TransactionalHelper;
import com.imcuttle.bar.message.event.RecordingApplicationUpdateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author lukebj
 */
@Service
public class RecordingApplicationUpdateEventProducer extends NormalAliMqProduceQueue<RecordingApplicationUpdateEvent> {

    @Value("${aliMq.producer.topic.recording.name}")
    private String topic;

    @Value("${aliMq.cluster}")
    private String aliMqCluster;

    @Override
    protected ObjectTransformer<RecordingApplicationUpdateEvent> initObjectTransformer() {
        return new JsonTransformer<>(RecordingApplicationUpdateEvent.class);
    }

    @Override
    protected String initTopic() {
        return topic;
    }

    @Override
    protected AlimqCluster initAliMqCluster() {
        return AlimqCluster.findByString(aliMqCluster);
    }

    @Override
    public void publish(RecordingApplicationUpdateEvent recordingApplicationUpdateEvent) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionalHelper.afterCommitSafe(() -> super.publish(recordingApplicationUpdateEvent, RecordingApplicationUpdateEvent.TAG_RECORDING_APPLICATION_UPDATED));
        } else {
            super.publish(recordingApplicationUpdateEvent, RecordingApplicationUpdateEvent.TAG_RECORDING_APPLICATION_UPDATED);
        }
    }

    public void publish(long applicationId) {
        RecordingApplicationUpdateEvent event = new RecordingApplicationUpdateEvent();
        event.setRecordingApplicationId(applicationId);
        publish(event);
    }
}
