/**
 * @(#)RecordingApplicationStorageImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db.impl;

import com.fenbi.common.db.mapper.LongRowMapper;
import com.imcuttle.bar.storage.db.BaseDbStorage;
import com.imcuttle.bar.storage.db.RecordingApplicationStorage;
import com.imcuttle.thrift.RecordingApplication;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linbonan
 */
@Repository
public class RecordingApplicationStorageImpl extends BaseDbStorage implements RecordingApplicationStorage {

    private static final String TABLE_NAME = "`recording_application`";

    private static final String COLUMNS_WITHOUT_ID = "`phaseId`,`subjectId`,`name`,`questionSource`,`estimatedQuestionNum`," +
            "`resourceId`,`fileName`,`remark`,`secrecyEndTime`,`submitTime`,`publishTime`,`creator`,`stage`";

    private static final String ALL_COLUMNS = "`id`, " + COLUMNS_WITHOUT_ID;

    private static final RowMapper<RecordingApplication> ROW_MAPPER = ((rs, rowNum) -> {
        RecordingApplication application = new RecordingApplication();
        application.setId(rs.getLong("id"));
        application.setPhaseId(rs.getInt("phaseId"));
        application.setSubjectId(rs.getInt("subjectId"));
        application.setName(rs.getString("name"));
        application.setQuestionSource(rs.getInt("questionSource"));
        application.setEstimatedQuestionNum(rs.getInt("estimatedQuestionNum"));
        application.setResourceId(rs.getString("resourceId"));
        application.setFileName(rs.getString("fileName"));
        application.setRemark(rs.getString("remark"));
        application.setSecrecyEndTime(rs.getLong("secrecyEndTime"));
        application.setSubmitTime(rs.getLong("submitTime"));
        application.setPublishTime(rs.getLong("publishTime"));
        application.setCreator(rs.getInt("creator"));
        application.setStage(rs.getInt("stage"));
        return application;
    });


    @Override
    public List<RecordingApplication> getByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }

        String sql = "SELECT " + ALL_COLUMNS + " FROM " + TABLE_NAME + " WHERE id in (:ids)";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("ids", ids);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public long create(RecordingApplication recordingApplication) {
        if (StringUtils.isBlank(recordingApplication.getRemark())) {
            recordingApplication.setRemark(StringUtils.EMPTY);
        }

        String sql = "INSERT INTO " + TABLE_NAME + " SET " +
                "`phaseId` = :phaseId, " +
                "`subjectId` = :subjectId, " +
                "`name` = :name, " +
                "`questionSource` = :questionSource, " +
                "`estimatedQuestionNum` = :estimatedQuestionNum, " +
                "`resourceId` = :resourceId, " +
                "`fileName` = :fileName, " +
                "`remark` = :remark, " +
                "`secrecyEndTime` = :secrecyEndTime, " +
                "`submitTime` = :submitTime, " +
                "`publishTime` = :publishTime, " +
                "`creator` = :creator, " +
                "`stage` = :stage";
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(recordingApplication);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        dbClient.update(sql, parameterSource, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    @Override
    public List<Long> batchCreate(Collection<RecordingApplication> recordingApplications) {
        if (CollectionUtils.isEmpty(recordingApplications)) {
            return Lists.newArrayList();
        }

        String sql = "INSERT INTO " + TABLE_NAME + " SET " +
                "`phaseId` = :phaseId, " +
                "`subjectId` = :subjectId, " +
                "`name` = :name, " +
                "`questionSource` = :questionSource, " +
                "`estimatedQuestionNum` = :estimatedQuestionNum, " +
                "`resourceId` = :resourceId, " +
                "`fileName` = :fileName, " +
                "`remark` = :remark, " +
                "`secrecyEndTime` = :secrecyEndTime, " +
                "`submitTime` = :submitTime, " +
                "`publishTime` = :publishTime, " +
                "`creator` = :creator, " +
                "`stage` = :stage";

        List<SqlParameterSource> parameterSources = recordingApplications.stream().map(BeanPropertySqlParameterSource::new).collect(Collectors.toList());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        dbClient.batchUpdateWithSqlParamSource(sql, parameterSources, keyHolder);

        List<Long> ids = keyHolder.getKeyList().stream()
                .map(fieldValueMap -> fieldValueMap.values().iterator().next())
                .filter(key -> key instanceof Number)
                .map(key -> ((Number) key).longValue())
                .collect(Collectors.toList());

        Preconditions.checkState(ids.size() == recordingApplications.size(), "批量创建录题申请失败");
        return ids;
    }

    @Override
    public boolean updateStage(long id, int fromStage, int toStage) {
        String sql = String.format("UPDATE %s SET stage = :targetStage WHERE id = :id AND stage = :currentStage", TABLE_NAME);
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("targetStage", toStage);
        parameterSource.addValue("currentStage", fromStage);
        parameterSource.addValue("id", id);
        return dbClient.update(sql, parameterSource) > 0;
    }

    @Override
    public boolean updateBasicInfo(RecordingApplication recordingApplication) {
        if (StringUtils.isBlank(recordingApplication.getRemark())) {
            recordingApplication.setRemark(StringUtils.EMPTY);
        }

        String sql = "UPDATE " + TABLE_NAME + " SET " +
                "`phaseId` = :phaseId, " +
                "`subjectId` = :subjectId, " +
                "`name` = :name, " +
                "`questionSource` = :questionSource, " +
                "`estimatedQuestionNum` = :estimatedQuestionNum, " +
                "`resourceId` = :resourceId, " +
                "`fileName` = :fileName, " +
                "`remark` = :remark, " +
                "`secrecyEndTime` = :secrecyEndTime, " +
                "`creator` = :creator " +
                "WHERE id = :id";

        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(recordingApplication);
        return dbClient.update(sql, parameterSource) > 0;
    }

    @Override
    public List<Long> scanId(long cursorIdExclusive, int batchSize) {
        String sql = "SELECT id FROM " + TABLE_NAME + " WHERE `id` > :cursorId ORDER BY `id` ASC LIMIT :batchSize";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("cursorId", cursorIdExclusive);
        parameterSource.addValue("batchSize", batchSize);
        return dbClient.query(sql, parameterSource, LongRowMapper.INSTANCE);
    }

    @Override
    public void updatePublishTime(RecordingApplication application) {
        String sql = String.format("UPDATE %s SET publishTime = :publishTime WHERE id = :id", TABLE_NAME);
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("publishTime", application.getPublishTime());
        parameterSource.addValue("id", application.getId());
        dbClient.update(sql, parameterSource);
    }
}
