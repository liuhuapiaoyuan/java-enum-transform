/**
 * @(#)FeatureVO.java, Apr 15, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author duanou
 */
@Data
public class FeatureVO {

    @ApiModelProperty("功能 key")
    private String key;

    @ApiModelProperty("角色列表")
    private List<RoleVO> roles;
}
