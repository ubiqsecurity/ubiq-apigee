
var dataset_definition = context.getVariable('private.dataset_def');
var dataset_ttl = context.getVariable('dataset_ttl');

// If Dataset Definition is older than TTL minutes, force a clean fetch
// Default to 30 minutes 
dataset_ttl = dataset_ttl ? dataset_ttl : 30;
var expire_after_seconds = 30 * 60

if (dataset_definition && dataset_definition.retrieved && ((Date.now()/1000) - dataset_definition.retrieved) > expire_after_seconds){
    print('cleared because of expiry, older than 30 minutes')
    context.setVariable('private.dataset_def', null)
    context.setVariable('debug.cleared_def', true)
}