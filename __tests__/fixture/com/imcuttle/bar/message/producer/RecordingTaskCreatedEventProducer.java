/**
 * @(#)RecordingTaskCreatedEventProducer.java, Apr 18, 2021.
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
import com.imcuttle.bar.message.event.RecordingTaskCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author tangjucai
 */
@Service
@Slf4j
public class RecordingTaskCreatedEventProducer extends NormalAliMqProduceQueue<RecordingTaskCreatedEvent> {

    @Value("${aliMq.producer.topic.recording.name}")
    private String topic;

    @Value("${aliMq.cluster}")
    private String aliMqCluster;

    @Override
    protected ObjectTransformer<RecordingTaskCreatedEvent> initObjectTransformer() {
        return new JsonTransformer<>(RecordingTaskCreatedEvent.class);
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
    public void publish(RecordingTaskCreatedEvent recordingTaskCreatedEvent) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionalHelper.afterCommitSafe(() -> doPublish(recordingTaskCreatedEvent));
        } else {
            doPublish(recordingTaskCreatedEvent);
        }
    }

    private void doPublish(RecordingTaskCreatedEvent recordingTaskCreatedEvent) {
        log.info("publish recordingTaskCreatedEvent = {}", recordingTaskCreatedEvent);
        super.publish(recordingTaskCreatedEvent, RecordingTaskCreatedEvent.TAG_RECORDING_TASK_CREATED);
    }
}
