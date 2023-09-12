#!/bin/bash

echo Using org and environment configured in /setup/setenv.sh

source ./setenv.sh

# Run without encryption
# curl http://$org-$env.$api_domain/ubiq-poc -H "Ubiq-Auth:true"

# Run with encryption
curl http://$org-$env.$api_domain/ubiq-poc



