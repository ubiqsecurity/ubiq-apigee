# Apigee Dataset Flow

This is a flow for accessing Ubiq Dataset definitions. When you want to use the Ubiq library, add this to your flow BEFORE the call to the Java library.

## Configuration
### Shared Flow
Create a ZIP archive containing the `sharedflowbundle` folder. (If this it is named anything else, Apigee will consider it an empty bundle and return an error.) On Apigee, click to create a new Shared Flow and select Upload. Name the flow apropriately. (We recommend `Ubiq Dataset Flow`)

### In Your Flow
Add a new Shared Flow policy. Select the flow you created in the previous step. You will need to add a new copy of the shared flow for each Dataset you wish to access. Update the Flow Callout XML to look like below for each dataset (for example, to access the definition needed to encrypt with the `BIRTH_DATE` dataset):
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<FlowCallout async="false" continueOnError="false" enabled="true" name="Get-BIRTH_DATE">
    <DisplayName>Get BIRTH_DATE</DisplayName>
    <Parameters>
        <Parameter name="ACCESS_KEY_ID">{private.ACCESS_KEY_ID}</Parameter>
        <Parameter name="SECRET_SIGNING_KEY">{private.SECRET_SIGNING_KEY}</Parameter>
        <Parameter name="dataset_name">BIRTH_DATE</Parameter>
        <Parameter name="dataset_ttl">30</Parameter>
        <Parameter name="save_decrypted_data_key">false</Parameter>
        <Parameter name="save_encrypted_data_key">false</Parameter>
    </Parameters>
    <SharedFlowBundle>Ubiq-Dataset-Flow</SharedFlowBundle>
</FlowCallout>
```
4 Key Parameters:
- **ACCESS_KEY_ID** - Your public access key (see your Ubiq Credentials). 
- **SECRET_SIGNING_KEY** - Your secret signing key, used to sign requests to the Ubiq API (see your Ubiq Credentials).
- **dataset_name** - Name of the Dataset you are wanting to work with
- **dataset_ttl** - Minutes to hold a dataset definition before pulling a fresh copy. Prevents stale definitions in case of rotation. Defaults to 30 minutes. 

> Note: It is recommend to store your credentials in your encrypted "Ubiq" Apigee KVM rather than hardcoding it in your policies as plain text. If the KVM is encrypted, the variable will need to be prefixed with `private.`

Optional Parameters:
- **save_decrypted_data_key**: Retrieves data keys from the Ubiq platform but saves a cached copy of the decrypted data key in the KVM for fastest throughput.
- **save_encrypted_data_keys**: Retrieves data keys from the Ubiq platform and stored a symmetrically encrypted version in the KVM.  This is significantly faster than storing an asymmetrically encrypted version of the data key in the KVM. This is only slightly slower than storing the decrypted version of the data key in the KVM.


### Output 
Output will be stored in two variables based on your dataset name: 
- `private.{dataset_name}.key` 
- `private.{dataset_name}.definition`. 

(Example: `private.BIRTH_DATE.key`) These will be private to prevent them from being shown when the flow is viewed in Apigee's trace functionality. 

