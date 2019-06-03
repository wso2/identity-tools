/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package secondaryUserStore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import secondaryUserStore.migrator.UserStorePasswordMigrator;

import static secondaryUserStore.util.Constant.JVM_PROPERTY_MIGRATE_PASSWORD;

@Component(
        name = "org.wso2.is.password.reencrypt",
        immediate = true
)
public class ISReEncryptionServiceComponent {

    private static final Log log = LogFactory.getLog(ISReEncryptionServiceComponent.class);

    /**
     * Method to activate bundle.
     * start with -DreEncryptSecondaryUserStorePassword to start the reEncryption
     * @param context OSGi component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            String migration = System.getProperty(JVM_PROPERTY_MIGRATE_PASSWORD);
            log.info("secondary userstore password re-encryption component activated");

            if (migration != null) {
                UserStorePasswordMigrator userStorePasswordMigrator = new UserStorePasswordMigrator();
                log.info("secondary userstore password re-encryption started");
                userStorePasswordMigrator.migrate();
                log.info("secondary userstore password re-encryption ended");
            }

        } catch (Throwable e) {
            log.error("Error while initiating Config component", e);
        }

    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("WSO2 IS migration bundle is deactivated");
        }
    }

    @Reference(
            name = "server.configuration.service",
            service = ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService"
    )
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        ISReEncryptionServiceDataHolder.setServerConfigurationService(serverConfigurationService);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        ISReEncryptionServiceDataHolder.setServerConfigurationService(null);
    }

}
