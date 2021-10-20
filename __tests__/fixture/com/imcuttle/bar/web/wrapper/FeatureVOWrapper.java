/**
 * @(#)FeatureVOWrapper.java, Apr 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.web.data.FeatureVO;
import com.imcuttle.bar.web.data.RoleAttributeVO;
import com.imcuttle.bar.web.data.RoleVO;
import com.fenbi.tutor.patronus.thrift.FeatureRoles;
import com.fenbi.tutor.patronus.thrift.Role;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author duanou
 */
public class FeatureVOWrapper {

    public static FeatureVO wrap(FeatureRoles featureRoles) {
        if (featureRoles == null || featureRoles.getFeature() == null) {
            return null;
        }

        FeatureVO featureVO = new FeatureVO();
        featureVO.setKey(featureRoles.getFeature().getKey());
        List<RoleVO> roleVOList = wrapRoleListToRoleVOList(featureRoles.getRoles());
        featureVO.setRoles(roleVOList);
        return featureVO;
    }

    private static List<RoleVO> wrapRoleListToRoleVOList(List<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return new ArrayList<>();
        }

        Map<String, List<Role>> basicRoleKeyRoleListMap = new HashMap<>();
        roles.forEach(role -> {
            String basicRoleKey = role.getBasicRoleKey();
            if (!basicRoleKeyRoleListMap.containsKey(basicRoleKey)) {
                basicRoleKeyRoleListMap.put(basicRoleKey, new ArrayList<>());
            }

            List<Role> currentBasicRoleKeyRoleList = basicRoleKeyRoleListMap.get(basicRoleKey);
            currentBasicRoleKeyRoleList.add(role);
        });

        List<RoleVO> result = new ArrayList<>();
        for (Entry<String, List<Role>> entry : basicRoleKeyRoleListMap.entrySet()) {
            RoleVO roleVO = new RoleVO();
            roleVO.setBasicRoleKey(entry.getKey());
            List<RoleAttributeVO> attributes = new ArrayList<>();
            roleVO.setAttributes(attributes);

            entry.getValue().forEach(role -> {
                RoleAttributeVO attributeVO = new RoleAttributeVO();
                attributeVO.setPhaseId(role.getPhaseId());
                attributeVO.setSubjectId(role.getSubjectId());
                attributes.add(attributeVO);
            });
            result.add(roleVO);
        }
        return result;
    }
}
