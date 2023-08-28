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
        <Parameter name="ffs_name">BIRTH_DATE</Parameter>
        <Parameter name="output_name">private.BIRTH_DATE</Parameter>
    </Parameters>
    <SharedFlowBundle>Ubiq-Dataset-Flow</SharedFlowBundle>
</FlowCallout>
```
4 Key Parameters:
- **ACCESS_KEY_ID** - Your public access key (see your Ubiq Credentials). 
- **SECRET_SIGNING_KEY** - Your secret signing key, used to sign requests to the Ubiq API (see your Ubiq Credentials).
- **ffs_name** - Name of the Dataset you are wanting to work with
- **output_name** - Name of the variable to store the dataset definition in. You will need to pass this name to the Ubiq library as well, so it knows where to retrieve the data.

> Note: It is recommend to store your credentials in your encrypted "Ubiq" Apigee KVM rather than hardcoding it in your policies as plain text. If the KVM is encrypted, the variable will need to be prefixed with `private.`


