# Ubiq Encryption in Apigee
The Ubiq Security Apigee library provides a convenient method of including the Ubiq Security Platform into your Apigee flows. Included are policies and code examples for setting up and performing encryption and decryption of data.

## Configuration
Create a Key Value Map within your Apigee environment with the name `Ubiq`. For security, we recommend enabling the Encryption on your KVM. It will prevent your data from being accessed from outside your flows once added. We also recommend adding your `ACCESS_KEY_ID`, `SECRET_SIGNING_KEY`, and `SECRET_CRYPTO_ACCESS_KEY` as entries. This way you do not need to hardcode them in your flow, only pull them from your KVM.

> Note: When accessing keys in an encrypted KVM, you must prefix your variable names with `private.` (eg. `private.ACCESS_KEY_ID`) or it will throw an error at runtime. For more information, view Apigee's Documentation on [Key Value Map Operations](https://docs.apigee.com/api-platform/reference/policies/key-value-map-operations-policy#getelement-attributes)

## Usage
For each Dataset you wish to use, you will need to add the Dataset Flow and configure it appropriately. See [dataset_flow/README.md](dataset_flow/README.md) for more information.

### Java Callout


## Example
