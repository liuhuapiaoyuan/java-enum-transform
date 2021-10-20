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
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author chenyibo
 */
@Service
@Slf4j
public class RecordingTaskUpdatedEventProducer extends NormalAliMqProduceQueue<RecordingTaskUpdatedEvent> {

    @Value("${aliMq.producer.topic.recording.name}")
    private String topic;

    @Value("${aliMq.cluster}")
    private String aliMqCluster;

    @Override
    protected ObjectTransformer<RecordingTaskUpdatedEvent> initObjectTransformer() {
        return new JsonTransformer<>(RecordingTaskUpdatedEvent.class);
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
    public void publish(RecordingTaskUpdatedEvent recordingTaskUpdatedEvent) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionalHelper.afterCommitSafe(() -> doPublish(recordingTaskUpdatedEvent));
        } else {
            doPublish(recordingTaskUpdatedEvent);
        }
    }

    private void doPublish(RecordingTaskUpdatedEvent recordingTaskUpdatedEvent) {
        log.info("publish RecordingTaskUpdatedEvent = {}", recordingTaskUpdatedEvent);
        super.publish(recordingTaskUpdatedEvent, RecordingTaskUpdatedEvent.TAG_RECORDING_TASK_UPDATED);
    }
}
