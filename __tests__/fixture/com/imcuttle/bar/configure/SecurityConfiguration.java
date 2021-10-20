/**
 * @(#)SecurityConfiguration.java, Apr 26, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.configure;

import com.fenbi.commons.security.UserRole;
import com.fenbi.commons2.rest.misc.SecurityConfigurer;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tangjucai
 */
@Configuration
public class SecurityConfiguration implements SecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        List<String> validRoles = Arrays.stream(TutorUserRole.INSECURE_ROLES).collect(Collectors.toList());
        validRoles.addAll(Arrays.asList(UserRole.USER_ROLES));
        validRoles.addAll(Arrays.asList(TrUserRole.ARMORY_USER_ROLES));
        http.authorizeRequests().antMatchers(TutorArmoryConstant.API + "/**")
                .hasAnyRole(validRoles.toArray(new String[]{}));
    }
}
