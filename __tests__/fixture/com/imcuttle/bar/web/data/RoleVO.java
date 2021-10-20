/**
 * @(#)RoleVO.java, Apr 15, 2021.
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
public class RoleVO {

    @ApiModelProperty("基础角色 key")
    private String basicRoleKey;

    @ApiModelProperty("角色属性")
    private List<RoleAttributeVO> attributes;
}
