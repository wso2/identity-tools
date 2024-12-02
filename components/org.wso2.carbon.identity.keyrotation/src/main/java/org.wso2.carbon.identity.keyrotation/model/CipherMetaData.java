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

package org.wso2.carbon.identity.keyrotation.model;

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;

/**
 * This class holds the cipher, transformation and initialization vector metadata.
 */
public class CipherMetaData {

    private String c;
    private String t = KeyRotationConstants.TRANSFORMATION;
    private String iv;
    // Key id which is used to determine which key was used to encrypt the secret.
    private String kid;

    /**
     * Get for the transformation.
     *
     * @return Set of operation to be performed on the given input to product some output.
     */
    public String getTransformation() {

        return t;
    }

    /**
     * Set for the transformation.
     *
     * @param transformation Set of operation to be performed on the given input to product some output.
     */
    public void setTransformation(String transformation) {

        this.t = transformation;
    }

    /**
     * Get for the ciphertext.
     *
     * @return The ciphertext.
     */
    public String getCipherText() {

        return c;
    }

    /**
     * Set for the ciphertext.
     *
     * @param cipher The ciphertext.
     */
    public void setCipherText(String cipher) {

        this.c = cipher;
    }

    /**
     * Get for the base64 decoded ciphertext.
     *
     * @return Base64 decoded cipher.
     */
    public byte[] getCipherBase64Decoded() {

        return Base64.decode(c);
    }

    /**
     * Get for the IV.
     *
     * @return Initialization vector.
     */
    public String getIv() {

        return iv;
    }

    /**
     * Set for the IV.
     *
     * @param iv Initialization vector.
     */
    public void setIv(String iv) {

        this.iv = iv;
    }

    /**
     * Get for the base64 decoded IV.
     *
     * @return Base64 decoded initialization vector.
     */
    public byte[] getIvBase64Decoded() {

        return Base64.decode(iv);
    }

    public void setKeyId(String kid) {

        this.kid = kid;
    }

    public String getKeyId() {

        return this.kid;
    }
}
