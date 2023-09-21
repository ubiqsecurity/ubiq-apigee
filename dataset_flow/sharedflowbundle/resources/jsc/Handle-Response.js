var dataset_name = context.getVariable('dataset_name');
var response = context.getVariable('responseDatasetKey.content');
var dataset_ttl = context.getVariable('dataset_ttl');

// Default TTL to 30 minutes 
dataset_ttl = dataset_ttl ? dataset_ttl : 30;
var expire_after_seconds = dataset_ttl * 60 * 1000;

// Parse if string
response = typeof response == "string" ? JSON.parse(response) : response;

// Handle Fresh Response
var full_dataset = response[dataset_name];

// Set date retrieved if missing
var now = Date.now();

var dataset_def = Object.assign({}, full_dataset.ffs);
dataset_def.retrieved = now;

var dataset_key = {
    "encrypted_private_key": full_dataset.encrypted_private_key,
    "key_number": full_dataset.current_key_number,
    "wrapped_data_key": full_dataset.keys[full_dataset.current_key_number],
    "retrieved": now,
    "expires": now + expire_after_seconds
}

context.setVariable('private.dataset_def', JSON.stringify(dataset_def));
context.setVariable('private.dataset_key', JSON.stringify(dataset_key));
