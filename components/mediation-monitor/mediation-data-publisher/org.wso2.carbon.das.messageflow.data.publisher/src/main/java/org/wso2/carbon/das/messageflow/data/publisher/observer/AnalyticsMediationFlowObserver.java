/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.das.messageflow.data.publisher.observer;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.wso2.carbon.das.messageflow.data.publisher.publish.StatisticsPublisher;
import org.wso2.carbon.context.PrivilegedCarbonContext;


public class AnalyticsMediationFlowObserver implements MessageFlowObserver,
                                                 TenantInformation {

    private static final Log log = LogFactory.getLog(AnalyticsMediationFlowObserver.class);
    private int tenantId = -1234;

    public AnalyticsMediationFlowObserver() {
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down the mediation statistics observer of DAS");
        }
    }

    @Override
    public void updateStatistics(PublishingFlow flow) {
        try {
            PrivilegedCarbonContext.startTenantFlow();

            // Using super tenant for all the publishing, data-bridge looks for thread-local tenantId
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            StatisticsPublisher.process(flow, tenantId);
        } catch (Exception e) {
            log.error("failed to update statics from DAS publisher", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(int i) {
        tenantId = i;
    }
}
