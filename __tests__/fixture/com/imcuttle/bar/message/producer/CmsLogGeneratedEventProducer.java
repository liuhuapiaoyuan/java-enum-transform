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
import com.imcuttle.bar.message.event.CmsLogGeneratedEvent;
import com.fenbi.tutor.cmslog.thrift.CmsLogEx;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * @author lukebj
 */
@Service
public class CmsLogGeneratedEventProducer extends NormalAliMqProduceQueue<CmsLogGeneratedEvent> {

    @Value("${aliMq.producer.topic.cmsLogGenerated.name}")
    private String topic;

    @Value("${aliMq.cluster}")
    private String aliMqCluster;

    @Override
    protected ObjectTransformer<CmsLogGeneratedEvent> initObjectTransformer() {
        return new JsonTransformer<>(CmsLogGeneratedEvent.class);
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
    public void publish(CmsLogGeneratedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionalHelper.afterCommitSafe(() -> super.publish(event, CmsLogGeneratedEvent.TAGS));
        } else {
            super.publish(event, CmsLogGeneratedEvent.TAGS);
        }
    }

    public void publish(CmsLogEx cmsLog) {
        CmsLogGeneratedEvent event = new CmsLogGeneratedEvent(cmsLog);
        publish(event);
    }

    public void publish(List<CmsLogEx> cmsLogs) {
        CmsLogGeneratedEvent event = new CmsLogGeneratedEvent(cmsLogs);
        publish(event);
    }
}
