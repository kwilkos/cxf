/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.service.invoker;

import java.beans.PropertyEditorSupport;

/**
 * This class is responsible for converting string to ScopePolicy object.
 * <p>
 * 
 * @author Ben Yu Feb 12, 2006 12:40:31 AM
 */
public class ScopePolicyEditor extends PropertyEditorSupport {
    /**
     * To get the default scope policy when no policy is specified. This
     * implementation uses "application" as default.
     */
    public static ScopePolicy getDefaultScope() {
        return ApplicationScopePolicy.instance();
    }

    /**
     * Convert a policy name to ScopePolicy object.
     * 
     * @param policy the policy name.
     * @return the ScopePolicy object.
     */
    public static ScopePolicy toScopePolicy(String policy) {
        if (policy == null) {
            return getDefaultScope();
        }

        policy = policy.trim();

        if (policy.length() == 0) {
            return getDefaultScope();
        } else if ("application".equals(policy)) {
            return ApplicationScopePolicy.instance();
        } else if ("session".equals(policy)) {
            return SessionScopePolicy.instance();
        } else if ("request".equals(policy)) {
            return RequestScopePolicy.instance();
        } else {
            throw new IllegalArgumentException("Scope " + policy + " is invalid.");
        }
    }

    public void setAsText(String text) {
        setValue(toScopePolicy(text));
    }
}
