# Apigee Sample Application

## Java Callout

This is the piece that responds to the Apigee workflow and calls the Ubiq libraries to encrypt the data.  This repo contains all the files necessary to run the sample application with the exception of editing the <b>setenv.sh</b> file referenced below.

## Requirement

Apigee uses Java 1.8 for the execution environment so all programs / libraries need to be developed for the the Java 1.8 environment.  Apigee has restrictions on the libraries that can be used / referenced by the callout application.  See the [Apigee documentation](https://cloud.google.com/apigee/docs/api-platform/reference/policies/java-callout-policy) for additional information.  There are also restrictions regarding the size of the jars that can be uploaded with the deploy script.

In addition to having an account within Apigee you will need to edit the setenv.sh for your environment.

## Program Flow

Apigee callout programs require a class which implements Execution and has the execute method.  The execute method accepts both a MessageContext and ExecutionContext.  The callout also expects certain variables to be set in the KVM in order to interact with the Ubiq Encryption libraries.

## Required Variables preset in the KVM

The following variables must be set in the Apigee KVM.  Because these are stored encrypted in the Apigee KVM, internally, they are accessed by prefixing the name 'private.'

```
 ACCESS_KEY_ID
 SECRET_CRYPTO_ACCESS_KEY
```

Within the 'execute' method, the following lines retrieve the values from the KVM
```
  ACCESS_KEY_ID = getVar(messageContext, "private.ACCESS_KEY_ID");
  SECRET_CRYPTO_ACCESS_KEY = getVar(messageContext, "private.SECRET_CRYPTO_ACCESS_KEY");
```

## Mapping dataset name to the json fields to encrypt

The apiproxy/policies/ubiq-poc.xml contains the DatasetsMappings which contains JSON representation of the mappings between dataset name and the field to encrypt

The example below shows that the json path containing the field birthDate should be encrypted using the BIRTH_DATE dataset.  Similarly, any field found with the name ssn should be encrypted using the SSN dataset.

```
[
   { "dataset" : "BIRTH_DATE", "json_path" : "$..birthDate"},
   { "dataset" : "SSN", "json_path" : "$..ssn"}
]
```
The Dataset mapping is read and processed into a hashmap containing the DATASET name as the key and the json_path as the value.

## Usage Reporting
The apiproxy/policies/ubiq-poc.xml contains setting to control the granular level of usage reporting.  All encrypt / decrypt operations for a dataset will be summed together within the granular level.  For example, When set to HOURS, a single count will be reported for all encrypt operations the same dataset operation within the same hour, XX:00:00 - XX:59:59.9999

```
        <Property name="usage_timestamp_reporting_granularity">HOURS</Property>
```

Valid values are shown below with NANOS being the default
- DAYS
- HALF_DAYS
- HOURS
- MINUTES
- SECONDS
- MILLIS
- NANOS

## Encryption flow

The data to encrypt is passed into the Java callout using the Message context.  The json is parsed and then the program iterates over the dataset mappings hashmap to encrypt all of the values found based on the json path and dataset.

The resulting json is then formatted and returns in the MessageContext.

# apiproxy policies and proxies

## The policies directory contains the following files

The <B>credentials.xml</B> contains the setup of retrieving variables from the encrypted KVM.

The <B>RetrieveBirthday.xml</B> is an example of the setup required by the shared flow in order to retrieve the BIRTH_DATE dataset from the Ubiq platform or read the values that are cached in the Apigee KVM.  See the description of the [Shared Flow](../dataset_flow/README.md#in-your-flow)

The <B>RetrieveSSN.xml</B> is an example of the setup required by the shared flow in order to retrieve the BIRTH_DATE dataset from the Ubiq platform or read the values that are cached in the Apigee KVM.

As described above, the <B>ubiq-poc.xml</B> contains the mappings between dataset name and the corresponding json path to the fields that require encryption.

## The proxy directory contains the following file

The <B>default.xml</B> contains the processing steps.  In this case, the setup of the credentials, checking for the BIRTH_DATE dataset, checking for the SSN dataset, and then invoking the ubiq-poc.

## The resources/java directory 

This directory contains the JAR files that are deployed to Apigee.  The necessary Ubiq libraries are already included in this directory as well as other libraries required by the sample application.  Some dependencies are automatically supplied by the Apigee environment and are not included in this directory to prevent conflicts.  Apigee has a limitation to the size of the resources/java directory.  See the [Apigee](https://docs.apigee.com/) documentation for additional information.

## Compiling

Requires a Java 1.8 environment.

Uses maven to compile and package the application.  

The deployment scripts <B>deploy.sh</B> require Python 2

```
 # Install library dependencies into the local maven cache.
$ ./buildsetup.sh


$ cd callout

# Only required if modifying the sample application.  Otherwise you can skip to the deploy and invoke scripts
$ mvn compile package

$ cd ..

# Requires python 2 for the next two steps
$ ./deploy.sh

$ ./invoke.sh
```

## Ubiq API Error Reference

Occasionally, you may encounter issues when interacting with the Ubiq API. 

| Status Code | Meaning | Solution |
|---|---|---|
| 400 | Bad Request | Check name of datasets and credentials are complete. |
| 401 | Authentication issue | Check you have the correct API keys, and it has access to the datasets you are using.  Check dataset name. |
| 426 | Upgrade Required | You are using an out of date version of the library, or are trying to use newer features not supported by the library you are using.  Update the library and try again.
| 429 | Rate Limited | You are performing operations too quickly. Either slow down, or contact support@ubiqsecurity.com to increase your limits. | 
| 500 | Internal Server Error | Something went wrong. Contact support if this persists.  | 
| 504 | Internal Error | Possible API key issue.  Check credentials or contact support.  | 
