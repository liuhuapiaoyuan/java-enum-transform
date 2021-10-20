/**
 * @(#)RecordingApplicationSearchParamVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.server.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linbonan
 */
@Data
public class RecordingApplicationBasicSearchParamVO {

    @ApiModelProperty("学段")
    private int phaseId = -1;

    @ApiModelProperty("学科")
    private int subjectId = -1;

    @ApiModelProperty("题目来源")
    private int questionSource;

    @ApiModelProperty("关键字")
    private String keyword;

    @ApiModelProperty("id")
    private long id;

    @ApiModelProperty("提交时间(起)")
    private long submitStartTime;

    @ApiModelProperty("提交时间(止)")
    private long submitEndTime;

    @ApiModelProperty("进度")
    private int stage;

    @ApiModelProperty("录题申请的创建人ID")
    private int creator;

    @ApiModelProperty("页码")
    private int page = 0;

    @ApiModelProperty("每页行数")
    private int pageSize = 20;

    @ApiModelProperty("排序规则")
    private String sort;
}
