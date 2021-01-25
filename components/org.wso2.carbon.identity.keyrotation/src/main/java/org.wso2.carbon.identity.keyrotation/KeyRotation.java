package org.wso2.carbon.identity.keyrotation;

import java.nio.charset.StandardCharsets;
import org.wso2.carbon.identity.keyrotation.util.*;

public class KeyRotation {

    public static void main(String[] args){
        try{
            try {
                byte[] ciphertext =
                        SymmetricKeyInternalCryptoProvider
                                .encrypt(new String("sampleText").getBytes(StandardCharsets.UTF_8),"AES/GCM" +
                        "/NoPadding",null,true);
                byte[] plainText =  SymmetricKeyInternalCryptoProvider.decrypt(ciphertext,"AES/GCM" +
                        "/NoPadding",null);
                System.out.println(plainText);
            }catch (CryptoException e){
                System.out.println("CRYPTO ERROR "+e);
            }
        }catch(Exception e){
            System.out.println("ERROR: "+e);
        }
    }

}
