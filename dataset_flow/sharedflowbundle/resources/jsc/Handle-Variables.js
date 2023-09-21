// Get the finalized names
var def_variable = context.getVariable('def_variable');
var key_variable = context.getVariable('key_variable');

// Get the content
var def_payload = context.getVariable('private.dataset_def')
var key_payload = context.getVariable('private.dataset_key')

// Outgoing variable for Java use
// `private.` to hide from trace
context.setVariable('private.' + def_variable, def_payload);
context.setVariable('private.' + key_variable, key_payload);
