package com.apigeesample;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.apigee.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import com.ubiqsecurity.UbiqCredentials;
import com.ubiqsecurity.UbiqConfiguration;
import com.ubiqsecurity.UbiqFPEEncryptDecrypt;
import com.ubiqsecurity.UbiqFactory;
import org.bouncycastle.crypto.InvalidCipherTextException;

import ubiqsecurity.fpe.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.internal.JsonFormatter;

import java.util.concurrent.*;

import java.time.Duration;
import java.time.Instant;

import com.google.gson.*;


public class UbiqDecrypt implements Execution {

  // private Map <String,String> properties; // read-only

  // public UbiqDecrypt(Map <String,String> properties) {
  //         this.properties = properties;
  // }

  // public String getProperty(String propName) throws IllegalArgumentException {
  //   String ret = null;
  //   Object obj = this.properties.get(propName);
  //   if (obj != null && obj instanceof String) {
  //     ret = (String)obj;
  //   }
  //   return ret;
  // }


  public String getVar(MessageContext messageContext, String varName) throws IllegalArgumentException {
      String ret = null;
      Object obj = messageContext.getVariable(varName);
      if (obj != null && obj instanceof String) {
        ret = (String)obj;
      } else if (obj == null) {
        // throw new IllegalArgumentException("'" + varName + "' is null");
       } else {
        ret = obj.toString();
      }
      return ret;
  }

	public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {
		
// messageContext.setVariable("definition_key", "HELLO WORLD");
// return ecutionResult.SUCCESS;

  try {

    UbiqCredentials ubiqCredentials;
    
    String ACCESS_KEY_ID = "";
    String SECRET_CRYPTO_ACCESS_KEY="";
    String SECRET_SIGNING_KEY="";
    String SERVER="";

    ACCESS_KEY_ID = getVar(messageContext, "private.ACCESS_KEY_ID");
    SECRET_CRYPTO_ACCESS_KEY = getVar(messageContext, "private.SECRET_CRYPTO_ACCESS_KEY");

    // Keep null to prevent billing record calls at end
    // SECRET_SIGNING_KEY = getVar(messageContext, "private.SECRET_SIGNING_KEY");

    String save_decrypted_data_key = getVar(messageContext,"save_decrypted_data_key");
    String save_encrypted_data_key = getVar(messageContext, "save_encrypted_data_key");

    String key = getVar(messageContext,"private.dataset_key");
    if ((key != null) && (save_decrypted_data_key != null || save_encrypted_data_key != null)) {
      ubiqCredentials = UbiqFactory.createCredentials(ACCESS_KEY_ID, SECRET_SIGNING_KEY, SECRET_CRYPTO_ACCESS_KEY, SERVER);

      UbiqConfiguration ubiqConfiguration = UbiqFactory.createConfiguration(5,5,5,true);

      try (UbiqFPEEncryptDecrypt ubiqEncryptDecrypt = new UbiqFPEEncryptDecrypt(ubiqCredentials, ubiqConfiguration)) {

          String decrypted_data_key = ubiqEncryptDecrypt.decryptKey(key);

          JsonParser parser = new JsonParser();
          JsonObject key_data = parser.parse(key).getAsJsonObject();
    
          if (save_decrypted_data_key != null && save_decrypted_data_key.equalsIgnoreCase("true")) {
            key_data.addProperty("decrypted_data_key", decrypted_data_key);
          } else if (save_encrypted_data_key != null && save_encrypted_data_key.equalsIgnoreCase("true")) {
            // MUST BE ENCRYPTED DAT A KEY - TODO
            // key_data.addProperty("encrypted_data_key", decrypted_data_key);
            key_data.add("encrypted_data_key", ubiqEncryptDecrypt.encryptData(decrypted_data_key.getBytes(), SECRET_CRYPTO_ACCESS_KEY));
          }

          messageContext.setVariable("private.dataset_key", key_data.toString());
        } // Try ubiqEncryptDecrypt
    }
    return ExecutionResult.SUCCESS;

  } catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);
        String st = sw.toString();
        String s = String.format("Exception: %s",e.getMessage());
        System.out.println(s);
        messageContext.getMessage().setContent(s+st);
        messageContext.setVariable("definition_key", s+st);
        return ExecutionResult.SUCCESS;
      }
   
  }

}