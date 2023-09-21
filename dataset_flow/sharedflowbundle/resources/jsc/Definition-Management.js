var dataset_def = context.getVariable('private.dataset_def');
var dataset_key = context.getVariable('private.dataset_key');

// In case it comes in as string instead of json payload
if(typeof dataset_def == "string"){
    dataset_def = JSON.parse(dataset_def)
}
if(typeof dataset_key == "string"){
    dataset_key = JSON.parse(dataset_key)
}

var valid_data = true;
// If outdated, clear the data to start a response
if (dataset_def && Date.now() > dataset_def.expires){
    print('Dataset definition is expired, cleared for fresh fetch.')
    context.setVariable('private.dataset_def', null)
    context.setVariable('debug.cleared', true)
    valid_data = false;
}
if (dataset_key && Date.now() > dataset_key.expires){
    print('Dataset key is expired, cleared for fresh fetch.')
    context.setVariable('private.dataset_key', null)
    context.setVariable('debug.cleared', true)
    valid_data = false;
}

var save_decrypted_data_key = context.getVariable('save_decrypted_data_key');
var save_encrypted_data_key = context.getVariable('save_encrypted_data_key');
if(valid_data && dataset_key && ((save_decrypted_data_key  && dataset_key.decrypted_data_key) || (save_encrypted_data_key && dataset_key.encrypted_data_key))){
    // Skip decrypting the data key, we have it decrypted appropriately.
    context.setVariable('data_key_ready', true)
} else {
    // Data key needs decrypted
    context.setVariable('data_key_ready', false)
}