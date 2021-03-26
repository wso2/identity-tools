/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.keyrotation.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static org.wso2.carbon.identity.keyrotation.util.EncryptionUtil.reEncryptor;

/**
 * The class that holds utility methods to update the configuration files.
 */
public class ConfigFileUtil {

    private static final Log log = LogFactory.getLog(ConfigFileUtil.class);

    /**
     * Get all the files inside the base path.
     *
     * @param isHome The absolute path of newISHome.
     * @param paths  A list of path segments.
     * @return List of files in path.
     */
    public static File[] getFilePaths(String isHome, String[] paths) {

        String path = Paths.get(isHome, paths).toString();
        File[] files = new File(path).listFiles();
        if (files != null) {
            return files;
        }
        return new File[0];
    }

    /**
     * Get all the tenants inside the tenant base path.
     *
     * @param isHome The absolute path of newISHome.
     * @return List of tenants inside the tenant base path.
     */
    public static List<String> getFolderPaths(String isHome) {

        String[] paths = new String[]{KeyRotationConstants.REPOSITORY, KeyRotationConstants.TENANTS};
        File[] listOfFolders = getFilePaths(isHome, paths);
        List<String> folderPaths = new ArrayList<>();
        for (File folder : listOfFolders) {
            folderPaths.add(folder.getName() + FileSystems.getDefault().getSeparator());
        }
        return folderPaths;
    }

    /**
     * Update the configuration file with the new re-encrypted value.
     *
     * @param filename          The absolute path of the configuration file.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @param property          The property value to identify the corresponding config file.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public static void updateConfigFile(File filename, KeyRotationConfig keyRotationConfig, String property)
            throws KeyRotationException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(filename);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList data =
                    (NodeList) xpath.compile("//" + property + "[@encrypted='true'][@name='password']/text()")
                            .evaluate(document, XPathConstants.NODESET);
            if (data.getLength() != 1 && data.getLength() != 0) {
                throw new KeyRotationException("Error occurred while updating config file having multiple encrypted " +
                        "properties");
            }
            if (data.getLength() == 1) {
                log.info("Re-encryption in " + filename + " configuration file.");
                String encryptedValue = data.item(0).getNodeValue();
                log.info("Encrypted value " + encryptedValue);
                String reEncryptedValue = reEncryptor(encryptedValue, keyRotationConfig);
                log.info("Re-encrypted value " + reEncryptedValue);
                data.item(0).setNodeValue(reEncryptedValue);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(document), new StreamResult(filename.getPath()));
            }
        } catch (SAXException | IOException e) {
            throw new KeyRotationException("Error occurred while parsing the xml file, " + e);
        } catch (TransformerException e) {
            throw new KeyRotationException("Error occurred while updating configuration file, " + e);
        } catch (ParserConfigurationException e) {
            throw new KeyRotationException("Error occurred while creating a new instance of DocumentBuilder, " + e);
        } catch (XPathExpressionException e) {
            throw new KeyRotationException("Error occurred in xPath expression, " + e);
        }
    }
}
