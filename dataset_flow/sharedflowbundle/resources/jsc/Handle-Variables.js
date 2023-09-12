var def_variable = context.getVariable('def_variable');
var key_variable = context.getVariable('key_variable');
var dataset_name = context.getVariable('dataset_name');

var response = context.getVariable('responseDatasetKey.content');

if(response){
    // Parse if string
    response = typeof response == "string" ? JSON.parse(response) : response;
    
    // Handle Fresh Response
    var full_dataset = response[dataset_name];

    // Set date retrieved if missing
    var now = Math.floor((Date.now()/1000));
    var date_retrieved = full_dataset && full_dataset.retrieved ? full_dataset.retrieved : now;
    
    var dataset_def = Object.assign({}, full_dataset.ffs);
    dataset_def.retrieved = date_retrieved;
    
    var dataset_key = {
        "encrypted_private_key": full_dataset.encrypted_private_key,
        "key_number": full_dataset.current_key_number,
        "wrapped_data_key": full_dataset.keys[full_dataset.current_key_number],
        "retrieved": date_retrieved
    }
    
    var def_payload = JSON.stringify(dataset_def)
    var key_payload = JSON.stringify(dataset_key)
    
    // Outgoing variable for Java use
    // `private.` to hide from trace
    context.setVariable('private.' + def_variable, def_payload);
    context.setVariable('private.' + key_variable, key_payload);
    // KVM content
    context.setVariable('private.dataset_def', def_payload);
    context.setVariable('private.dataset_key', key_payload);
} else {
    // Handle Existing Response
    var def_payload = context.getVariable('private.dataset_def')
    var key_payload = context.getVariable('private.dataset_key')
    // Outgoing variable for Java use
    // `private.` to hide from trace
    context.setVariable('private.' + def_variable, def_payload);
    context.setVariable('private.' + key_variable, key_payload);
}