/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.task.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.service.TaskManagementService;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.task.JobMetaDataProviderServiceHandler;
import org.wso2.carbon.task.service.TaskDeployerService;
import org.wso2.carbon.task.service.TaskDeployerServiceImpl;
import org.wso2.carbon.task.TaskManagementServiceHandler;
import org.wso2.carbon.task.TaskManager;
import org.wso2.carbon.task.multitenancy.TenantCreationListener;
import org.wso2.carbon.task.util.ConfigHolder;
import org.wso2.carbon.task.services.JobMetaDataProviderService;
import org.wso2.carbon.task.services.TaskDescriptionRepositoryService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "task.manager.componenet",
        immediate = true)
public class TaskManagementServiceComponent {

    private static final Log log = LogFactory.getLog(TaskManagementServiceComponent.class);

    private ConfigurationContextService configCtxSvc;

    private final JobMetaDataProviderServiceHandler jobMetaDataProviderServiceHandler = new
            JobMetaDataProviderServiceHandler();

    private final TaskManagementServiceHandler taskManagementServiceHandler = new TaskManagementServiceHandler();

    private TaskDescriptionRepositoryService repositoryService;

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initiating a TaskManager");
            }
            ConfigHolder.getInstance().setJobMetaDataProviderServiceHandler(jobMetaDataProviderServiceHandler);
            ConfigHolder.getInstance().setTaskManagementServiceHandler(taskManagementServiceHandler);
            // register the tenant creation listener
            TenantCreationListener listener = new TenantCreationListener();
            ctxt.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(), listener, null);
            ctxt.getBundleContext().registerService(TaskDeployerService.class.getName(), new TaskDeployerServiceImpl
                    (), null);
            // initialize the task manager
            TaskManager taskManager = new TaskManager();
            taskManager.setTaskDescriptionRepository(repositoryService.getTaskDescriptionRepository());
            taskManager.init(jobMetaDataProviderServiceHandler, taskManagementServiceHandler);
            this.configCtxSvc.getServerConfigContext().setProperty(TaskManager.CARBON_TASK_MANAGER, taskManager);
            this.configCtxSvc.getServerConfigContext().setProperty(TaskManager.CARBON_TASK_REPOSITORY,
                    repositoryService.getTaskDescriptionRepository());
            this.configCtxSvc.getServerConfigContext().setProperty(TaskManager.CARBON_TASK_JOB_METADATA_SERVICE,
                    jobMetaDataProviderServiceHandler);
            this.configCtxSvc.getServerConfigContext().setProperty(TaskManager.CARBON_TASK_MANAGEMENT_SERVICE,
                    taskManagementServiceHandler);
        } catch (Throwable t) {
            log.fatal("Error occured while initializing task management", t);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

    }

    @Reference(
            name = "job.metadata.provider.service",
            service = org.wso2.carbon.task.services.JobMetaDataProviderService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetJobMetaDataProviderService")
    protected void setJobMetaDataProviderService(JobMetaDataProviderService jobMetaDataProviderService) {

        if (log.isDebugEnabled()) {
            log.debug("Adding a JobMetaDataProviderService");
        }
        this.jobMetaDataProviderServiceHandler.addService(jobMetaDataProviderService);
    }

    protected void unsetJobMetaDataProviderService(JobMetaDataProviderService jobMetaDataProviderService) {

        this.jobMetaDataProviderServiceHandler.removeService(jobMetaDataProviderService);
    }

    @Reference(
            name = "task.management.service",
            service = org.apache.synapse.task.service.TaskManagementService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskManagementService")
    protected void setTaskManagementService(TaskManagementService taskManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("Adding a TaskManagementService");
        }
        this.taskManagementServiceHandler.addService(taskManagementService);
    }

    protected void unsetTaskManagementService(TaskManagementService taskManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("Removing a TaskManagementService");
        }
        this.taskManagementServiceHandler.removeService(taskManagementService);
    }

    @Reference(
            name = "task.description.repository.service",
            service = org.wso2.carbon.task.services.TaskDescriptionRepositoryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskDescriptionRepositoryService")
    protected void setTaskDescriptionRepositoryService(TaskDescriptionRepositoryService repositoryService) {

        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService  bound to the ESB initialization process");
        }
        this.repositoryService = repositoryService;
    }

    protected void unsetTaskDescriptionRepositoryService(TaskDescriptionRepositoryService repositoryService) {

        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService  unbound from the ESB environment");
        }
        this.repositoryService = null;
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the ESB initialization process");
        }
        this.configCtxSvc = configurationContextService;
        ConfigHolder.getInstance().setConfigCtxSvc(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the ESB environment");
        }
        this.configCtxSvc = null;
        ConfigHolder.getInstance().setConfigCtxSvc(null);
    }
}
