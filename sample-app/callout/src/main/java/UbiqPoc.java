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
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.internal.JsonFormatter;

import java.util.concurrent.*;

public class UbiqPoc implements Execution {

  private Map <String,String> properties; // read-only

  public UbiqPoc(Map <String,String> properties) {
          this.properties = properties;
  }

  public String getVar(MessageContext messageContext, String varName) throws IllegalArgumentException {
      String ret = null;
      Object obj = messageContext.getVariable(varName);
      if (obj != null && obj instanceof String) {
        ret = (String)obj;
      } else if (obj == null) {
        throw new IllegalArgumentException("'" + varName + "' is null");
       } else {
        ret = obj.toString();
      }
      return ret;
    }

  public String getProperty(String propName) throws IllegalArgumentException {
        String ret = null;
        Object obj = this.properties.get(propName);
        if (obj != null && obj instanceof String) {
          ret = (String)obj;
        } else {
          throw new IllegalArgumentException("Unable to find property '" + propName + "'");
        }
        return ret;
      }

	public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {
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

    String DatasetMappings = getProperty("DatasetsMappings");

    Object json_Datasets = new JSONParser().parse(DatasetMappings);
    JSONArray ja = (JSONArray)json_Datasets;

    Iterator itr2 = ja.iterator();
    HashMap<String, String> mappings = new HashMap<String, String>();

    while (itr2.hasNext()) 
        {
          String dataset_name = null;
          String json_path = null;

          Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
          while (itr1.hasNext()) {
            Map.Entry pair = (Map.Entry)itr1.next();
            if (pair.getKey().equals("dataset") ) {
              dataset_name = (String)pair.getValue();
            } else if (pair.getKey().equals("json_path") ) {
              json_path = (String)pair.getValue();
            }
          }
           mappings.put(dataset_name, json_path);
        }

    ubiqCredentials = UbiqFactory.createCredentials(ACCESS_KEY_ID, SECRET_SIGNING_KEY, SECRET_CRYPTO_ACCESS_KEY, SERVER);
    UbiqConfiguration ubiqConfiguration = UbiqFactory.createConfiguration(5,5,5,true);

    byte[] tweak = null;

    String ret = "";
    try (UbiqFPEEncryptDecrypt ubiqEncryptDecrypt = new UbiqFPEEncryptDecrypt(ubiqCredentials, ubiqConfiguration)) {

      String json = getVar(messageContext,"response_json");

      Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
      DocumentContext ctx = JsonPath.using(conf).parse(json);

      for (Map.Entry<String, String> entry : mappings.entrySet()) {
        String dataset_def = getVar(messageContext, "private." + entry.getKey() + ".definition");
        String name = ubiqEncryptDecrypt.loadDataset(dataset_def);
        String key = getVar(messageContext,"private." + entry.getKey() + ".key");

        // This assumes the key is CURRENT_KEY
        ubiqEncryptDecrypt.loadKeyDef(name, key, true);

        ctx.map(entry.getValue(), (currentValue, configuration) -> {
          String e = ubiqEncryptDecrypt.encryptFPE(name, (String)currentValue, tweak);
          return e;
        });
      }

      ret = JsonFormatter.prettyPrint(ctx.jsonString());
    }

    messageContext.getMessage().setContent(ret);
          
    return ExecutionResult.SUCCESS;

		} catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);
        String st = sw.toString();
        String s = String.format("Exception: %s",e.getMessage());
        System.out.println(s);
        messageContext.getMessage().setContent(s+st);
        return ExecutionResult.SUCCESS;
      }
    }
}
