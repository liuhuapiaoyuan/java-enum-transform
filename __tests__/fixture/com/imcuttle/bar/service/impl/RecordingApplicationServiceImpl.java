/**
 * @(#)RecordingApplicationServiceImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.common.lambda.Lambdas;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.bar.message.producer.RecordingApplicationCreatedEventProducer;
import com.imcuttle.bar.message.producer.RecordingApplicationUpdateEventProducer;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.storage.db.RecordingApplicationStorage;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingTask;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class RecordingApplicationServiceImpl implements RecordingApplicationService {

    @Autowired
    private RecordingApplicationStorage recordingApplicationStorage;

    @Autowired
    private RecordingApplicationCreatedEventProducer recordingApplicationCreatedEventProducer;

    @Autowired
    private RecordingApplicationUpdateEventProducer recordingApplicationUpdateEventProducer;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createRecordingTasksForApplication(long applicationId, List<RecordingTask> tasks) {
        boolean update = this.updateStage(applicationId, RecordingApplicationStage.TO_BE_ASSIGNED, RecordingApplicationStage.PROCESSING);
        if (!update) {
            return new ArrayList<>();
        }
        List<Long> recordingTaskIds = recordingTaskService.create(tasks);
        if (CollectionUtils.isEmpty(recordingTaskIds)) {
            log.error("failed to create recordingTasks: {}", tasks);
            throw new RuntimeException();
        }
        recordingApplicationUpdateEventProducer.publish(applicationId);
        return recordingTaskIds;
    }

    @Override
    public Map<Long, RecordingApplication> getByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }

        return recordingApplicationStorage.getByIds(ids)
                .stream()
                .collect(Collectors.toMap(RecordingApplication::getId, Function.identity(), Lambdas.pickLast()));
    }

    @Override
    public long create(RecordingApplication recordingApplication) {
        recordingApplication.setStage(RecordingApplicationStage.SUBMITTED.toInt());
        long id = recordingApplicationStorage.create(recordingApplication);
        log.info("创建录题申请 {}, recordingApplication = {}", id, recordingApplication);
        recordingApplicationCreatedEventProducer.publish(id);
        return id;
    }

    @Override
    public boolean updateStage(long applicationId, RecordingApplicationStage fromStage, RecordingApplicationStage targetStage) {
        boolean updated = recordingApplicationStorage.updateStage(applicationId, fromStage.toInt(), targetStage.toInt());
        log.info("更新录题申请状态到 = {}, success = {}", targetStage, updated);
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBasicInfo(RecordingApplication recordingApplication) {
        long applicationId = recordingApplication.getId();
        RecordingApplication current = recordingApplicationStorage.getByIds(Lists.newArrayList(applicationId))
                .stream()
                .filter(record -> record.getId() == applicationId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("要修改的录题申请不存在"));

        if (current.getStage() == RecordingApplicationStage.TO_BE_REVISED.toInt()) {
            boolean hasFeedback = recordingFeedbackService.
                    getFeedBacksOfSpecificTarget(applicationId, FeedbackTargetType.APPLICATION, true).size() > 0;
            if (hasFeedback) {
                boolean feedbackProcessed = recordingFeedbackService.processAll(applicationId, FeedbackTargetType.APPLICATION);
                Preconditions.checkState(feedbackProcessed, "更新录题申请下的反馈记录失败");
            }
            boolean stageUpdated = recordingApplicationStorage.updateStage(applicationId,
                    RecordingApplicationStage.TO_BE_REVISED.toInt(), RecordingApplicationStage.SUBMITTED.toInt());
            Preconditions.checkState(stageUpdated, "更新被驳回录题申请的状态失败");
        }

        log.info("更新录题申请基础信息，application = {}", recordingApplication);
        boolean updated = recordingApplicationStorage.updateBasicInfo(recordingApplication);
        Preconditions.checkState(updated, "更新录题申请基础信息失败");
        recordingApplicationUpdateEventProducer.publish(applicationId);
    }

    @Override
    public void updatePublishTime(RecordingApplication application) {
        log.info("更新发布时间，application = {}", application);
        recordingApplicationStorage.updatePublishTime(application);
    }

    @Override
    public List<Long> scanId(long cursorIdExclusive, int batchSize) {
        return recordingApplicationStorage.scanId(cursorIdExclusive, batchSize);
    }
}
