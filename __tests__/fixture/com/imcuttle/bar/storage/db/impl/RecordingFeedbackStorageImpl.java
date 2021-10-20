/**
 * @(#)RecordingFeedbackStorageImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fenbi.common.util.JsonUtils;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.data.QuestionRecordingTaskPair;
import com.imcuttle.bar.storage.db.BaseDbStorage;
import com.imcuttle.bar.storage.db.RecordingFeedbackStorage;
import com.imcuttle.thrift.RecordingFeedback;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.imcuttle.thrift.TutorArmoryConstants.LONG_ID_NO_LIMIT;

/**
 * @author linbonan
 */
@Repository
public class RecordingFeedbackStorageImpl extends BaseDbStorage implements RecordingFeedbackStorage {

    private static final String TABLE_NAME = "`recording_feedback`";

    private static final String COLUMNS_WITHOUT_ID = "`applicationId`,`taskId`, `targetId`,`targetType`,`type`" +
            ",`reason`,`description`,`attachments`,`createTime`,`creator`,`deleted`,`processed`, `examPaperId`";

    private static final String ALL_COLUMNS = "`id`, " + COLUMNS_WITHOUT_ID;

    private static final RowMapper<RecordingFeedback> ROW_MAPPER = ((rs, rowNum) -> {
        RecordingFeedback feedback = new RecordingFeedback();
        feedback.setId(rs.getLong("id"));
        feedback.setApplicationId(rs.getLong("applicationId"));
        feedback.setTaskId(rs.getLong("taskId"));
        feedback.setTargetId(rs.getLong("targetId"));
        feedback.setTargetType(rs.getInt("targetType"));
        feedback.setExamPaperId(rs.getLong("examPaperId"));
        feedback.setType(rs.getInt("type"));

        String reasonJson = rs.getString("reason");
        if (StringUtils.isNotBlank(reasonJson)) {
            feedback.setReason(JsonUtils.readValue(reasonJson, new TypeReference<List<String>>() {
            }));
        } else {
            feedback.setReason(Lists.newArrayList());
        }
        feedback.setDescription(rs.getString("description"));
        feedback.setCreateTime(rs.getLong("createTime"));
        feedback.setCreator(rs.getInt("creator"));
        feedback.setProcessed(rs.getBoolean("processed"));
        String attachmentStr = rs.getString("attachments");
        if (StringUtils.isBlank(attachmentStr)) {
            feedback.setAttachments(Lists.newArrayList());
        } else {
            feedback.setAttachments(JsonUtils.readValue(attachmentStr, new TypeReference<List<String>>() {
            }));
        }
        return feedback;
    });

    @Override
    public List<RecordingFeedback> getByTargetIdAndTargetTypeConditionally(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId, boolean onlyUnProcessed) {
        if (Objects.isNull(targetType)) {
            return Lists.newArrayList();
        }

        String sql = String.format("SELECT %s FROM %s WHERE targetType = :targetType AND targetId = :targetId AND deleted=0 ", ALL_COLUMNS, TABLE_NAME);
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("targetId", targetId);
        parameterSource.addValue("targetType", targetType.toInt());

        if (onlyUnProcessed) {
            sql = sql + " AND processed=0 ";
        }

        if (taskId != LONG_ID_NO_LIMIT) {
            sql = sql + " AND taskId = :taskId";
            parameterSource.addValue("taskId", taskId);
        }

        if (examPaperId != LONG_ID_NO_LIMIT) {
            sql = sql + " AND examPaperId = :examPaperId";
            parameterSource.addValue("examPaperId", examPaperId);
        }

        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public long create(RecordingFeedback recordingFeedback) {
        String sql = "INSERT INTO " + TABLE_NAME + " SET " +
                "`applicationId` = :applicationId, " +
                "`taskId` = :taskId, " +
                "`targetId` = :targetId, " +
                "`targetType` = :targetType, " +
                "`examPaperId` = :examPaperId, " +
                "`type` = :type, " +
                "`reason` = :reason, " +
                "`description` = :description, " +
                "`attachments` = :attachments, " +
                "`createTime` = :createTime, " +
                "`creator` = :creator";
        MapSqlParameterSource source = getMapSqlParameterSource(recordingFeedback, System.currentTimeMillis());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        dbClient.update(sql, source, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    private MapSqlParameterSource getMapSqlParameterSource(RecordingFeedback recordingFeedback, long createdTime) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("applicationId", recordingFeedback.getApplicationId());
        parameterSource.addValue("taskId", recordingFeedback.getTaskId());
        parameterSource.addValue("targetId", recordingFeedback.getTargetId());
        parameterSource.addValue("targetType", recordingFeedback.getTargetType());
        parameterSource.addValue("examPaperId", recordingFeedback.getExamPaperId());
        parameterSource.addValue("type", recordingFeedback.getType());
        parameterSource.addValue("reason", recordingFeedback.getReason() == null ? "[]"
                : JsonUtils.writeValue(recordingFeedback.getReason()));
        parameterSource.addValue("description", recordingFeedback.getDescription() == null ? "" : recordingFeedback.getDescription());
        parameterSource.addValue("attachments", recordingFeedback.getAttachments() == null ? "[]"
                : JsonUtils.writeValue(recordingFeedback.getAttachments()));
        parameterSource.addValue("createTime", createdTime);
        parameterSource.addValue("creator", recordingFeedback.getCreator());
        parameterSource.addValue("processed", recordingFeedback.isProcessed());
        return parameterSource;
    }

    @Override
    public List<Long> batchCreateWithProcessedAndCreatedTime(Collection<RecordingFeedback> recordingFeedbacks) {
        if (CollectionUtils.isEmpty(recordingFeedbacks)) {
            return Lists.newArrayList();
        }

        String sql = "INSERT INTO " + TABLE_NAME + " SET " +
                "`applicationId` = :applicationId, " +
                "`taskId` = :taskId, " +
                "`targetId` = :targetId, " +
                "`targetType` = :targetType, " +
                "`examPaperId` = :examPaperId, " +
                "`type` = :type, " +
                "`reason` = :reason, " +
                "`description` = :description, " +
                "`attachments` = :attachments, " +
                "`createTime` = :createTime, " +
                "`creator` = :creator, " +
                "`processed` = :processed";

        List<SqlParameterSource> parameterSources = recordingFeedbacks.stream()
                .map((RecordingFeedback recordingFeedback) -> getMapSqlParameterSource(recordingFeedback, recordingFeedback.getCreateTime()))
                .collect(Collectors.toList());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        dbClient.batchUpdateWithSqlParamSource(sql, parameterSources, keyHolder);

        List<Long> ids = keyHolder.getKeyList().stream()
                .map(fieldValueMap -> fieldValueMap.values().iterator().next())
                .filter(key -> key instanceof Number)
                .map(key -> ((Number) key).longValue())
                .collect(Collectors.toList());

        Preconditions.checkState(ids.size() == recordingFeedbacks.size(), "批量创建错题反馈失败");
        return ids;
    }

    @Override
    public List<RecordingFeedback> getUnprocessedFeedBacksOfRecordingTask(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId in (:taskIds) AND targetType = :targetType AND deleted = 0 AND processed = 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("taskIds", taskIds)
                .addValue("targetType", FeedbackTargetType.RECORDING_TASK.toInt());
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public boolean processAll(long targetId, FeedbackTargetType targetType) {
        String sql = "UPDATE " + TABLE_NAME + " SET processed=1 WHERE targetType = :targetType AND targetId = :targetId AND deleted=0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("targetType", targetType.toInt());
        parameterSource.addValue("targetId", targetId);
        return dbClient.update(sql, parameterSource) > 0;
    }

    @Override
    public List<RecordingFeedback> getAllTypeUnprocessedFeedBacksByTaskIds(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId in (:taskIds) AND deleted = 0 AND processed = 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("taskIds", taskIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public boolean update(RecordingFeedback feedback) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                "`type` = :type, " +
                "`reason` = :reason, " +
                "`description` = :description, " +
                "`attachments` = :attachments, " +
                "`createTime` = :createTime, " +
                "`creator` = :creator " +
                "WHERE id = :id";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("id", feedback.getId());
        parameterSource.addValue("type", feedback.getType());
        parameterSource.addValue("reason", feedback.getReason() == null ? "[]"
                : JsonUtils.writeValue(feedback.getReason()));
        parameterSource.addValue("description", feedback.getDescription() == null ? "" : feedback.getDescription());
        parameterSource.addValue("attachments", feedback.getAttachments() == null ? "[]"
                : JsonUtils.writeValue(feedback.getAttachments()));
        parameterSource.addValue("createTime", feedback.getCreateTime());
        parameterSource.addValue("creator", feedback.getCreator());

        return dbClient.update(sql, parameterSource) > 0;
    }

    @Override
    public List<RecordingFeedback> batchGetFeedBacks(List<Long> targetIds, FeedbackTargetType targetType, long taskId, boolean onlyUnProcessed) {
        if (CollectionUtils.isEmpty(targetIds)) {
            return Lists.newArrayList();
        }
        String sql = String.format("SELECT %s FROM %s WHERE targetType = :targetType AND targetId IN (:targetIds) AND deleted = 0 ", ALL_COLUMNS, TABLE_NAME);
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("targetIds", targetIds);
        parameterSource.addValue("targetType", targetType.toInt());

        if (onlyUnProcessed) {
            sql = sql + " AND processed = 0 ";
        }

        if (taskId != LONG_ID_NO_LIMIT) {
            sql = sql + " AND taskId = :taskId";
            parameterSource.addValue("taskId", taskId);
        }

        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public Map<Long, RecordingFeedback> getByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id in (:ids)";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("ids", ids);
        return dbClient.query(sql, parameterSource, ROW_MAPPER).stream()
                .collect(Collectors.toMap(RecordingFeedback::getId, Function.identity()));
    }

    @Override
    public boolean delete(long id) {
        String sql = "UPDATE " + TABLE_NAME + " SET deleted = 1 WHERE id = :id";
        return dbClient.update(sql, new MapSqlParameterSource("id", id)) > 0;
    }

    @Override
    public boolean batchUpdateProcessStatus(List<RecordingFeedback> feedbacks) {
        if (CollectionUtils.isEmpty(feedbacks)) {
            return true;
        }

        String sql = String.format("UPDATE %s SET " +
                "processed = :processed " +
                "WHERE id = :id", TABLE_NAME);
        List<SqlParameterSource> params = feedbacks.stream().map(feedback -> {
            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("processed", feedback.isProcessed() ? 1 : 0);
            param.addValue("id", feedback.getId());
            return param;
        }).collect(Collectors.toList());
        int[] results = dbClient.batchUpdateWithSqlParamSource(sql, params);
        return Arrays.stream(results).allMatch(result -> result > 0);
    }

    @Override
    public boolean deleteFeedBackOfQuestions(Collection<QuestionRecordingTaskPair> questionIdentifiers) {
        if (CollectionUtils.isEmpty(questionIdentifiers)) {
            return true;
        }

        String sql = "UPDATE " + TABLE_NAME + " SET deleted = 1 " +
                "WHERE taskId = :taskId " +
                "AND examPaperId = :examPaperId " +
                "AND targetId = :questionId " +
                "AND targetType = " + FeedbackTargetType.QUESTION.toInt();

        List<SqlParameterSource> parameterSources = questionIdentifiers
                .stream()
                .map(questionIdentifier -> {
                    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
                    parameterSource.addValue("taskId", questionIdentifier.getRecordingTaskId());
                    parameterSource.addValue("examPaperId", questionIdentifier.getExamPaperId());
                    parameterSource.addValue("questionId", questionIdentifier.getQuestionId());
                    return parameterSource;
                })
                .collect(Collectors.toList());
        int[] results = dbClient.batchUpdateWithSqlParamSource(sql, parameterSources);
        return Arrays.stream(results).allMatch(result -> result > 0);
    }

    @Override
    public boolean processAll(long taskId) {
        String sql = "UPDATE " + TABLE_NAME + " SET processed = 1 WHERE taskId = :taskId AND deleted = 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("taskId", taskId);
        return dbClient.update(sql, parameterSource) > 0;
    }

    @Override
    public boolean processAllByApplicationId(long applicationId) {
        String sql = "UPDATE " + TABLE_NAME + " SET processed = 1 WHERE applicationId = :applicationId AND deleted = 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("applicationId", applicationId);
        return dbClient.update(sql, parameterSource) > 0;
    }
}
