/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 */

package org.wso2.carbon.mediation.registry;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Properties;
import javax.activation.DataHandler;

public class TestMicroIntegratorRegistry {

    private static MicroIntegratorRegistry microIntegratorRegistry;
    private static Path governanceRegistry;

    @BeforeClass
    public static void init() throws IOException {

        microIntegratorRegistry = new MicroIntegratorRegistry();

        governanceRegistry = Paths.get("src", "test", "resources", "registry", "governance").toAbsolutePath();
        Files.createDirectories(governanceRegistry);

        Properties properties = new Properties();
        properties.setProperty(ESBRegistryConstants.GOV_REG_ROOT, governanceRegistry.toUri().toURL().toString());
        microIntegratorRegistry.init(properties);
    }

    @Before
    public void deployRegistryResource() {

        String filePath = "gov:/custom/checkJsScript.js";
        String content = "function checkLog(mc) {\n"
                         + "\tvar log = mc.getServiceLog();\n"
                         + "\tlog.info(\"Logging inside Script Mediator\");\n"
                         + "}\n";

        microIntegratorRegistry.newNonEmptyResource(filePath, false, "application/javascript", content, "");
    }

    @Test
    public void TestRegistryResourceDeployment() throws IOException {

        File resourceFile = Paths.get(governanceRegistry.toString(), "custom", "checkJsScript.js").toFile();
        Assert.assertTrue("checkJsScript.js file should be created", resourceFile.exists());

        File metadataFile =
                Paths.get(governanceRegistry.toString(), "custom", ".metadata", "checkJsScript.js.meta").toFile();
        Assert.assertTrue(".metadata/checkJsScript.js.meta file should be created", metadataFile.exists());

        Properties metadata = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
            metadata.load(reader);
        }
        String mediaType = metadata.getProperty("mediaType");
        Assert.assertEquals("Media type should be as expected", "application/javascript", mediaType);
    }

    @Test
    public void TestRegistryResourceRead() throws IOException {

        OMNode omNode = microIntegratorRegistry.lookup("gov:/custom/checkJsScript.js");
        String mediaType = ((DataHandler) ((OMTextImpl) omNode).getDataHandler()).getContentType();
        Assert.assertEquals("Media type should be as expected", "application/javascript", mediaType);
    }

    @AfterClass
    public static void cleanup() throws IOException {
        Files.walk(Paths.get(governanceRegistry.getParent().toString()))
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .forEach(File::delete);
    }
}
