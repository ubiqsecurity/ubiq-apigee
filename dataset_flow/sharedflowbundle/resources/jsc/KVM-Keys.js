// Get dataset name
var dataset_name = context.getVariable('dataset_name');

// Build the keys for the KVM
// eg, dataset_name BIRTH_DATE => BIRTH_DATE.definition and BIRTH_DATE.key
var def_variable = dataset_name + ".definition"
var key_variable = dataset_name + ".key"

// Store it in variables for KVM PUT/GET
context.setVariable('def_variable', def_variable);
context.setVariable('key_variable', key_variable);