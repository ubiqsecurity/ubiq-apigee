#!/bin/bash

source ./setenv.sh

echo "Enter your password for the Apigee Enterprise organization $org, followed by [ENTER]:"

#read -s password
if [ -z "$password" ]; then
  read -s password
fi


echo Deploying $proxy to $env on $url using $username and $org

./deploy.py -n ubiq-poc -u $username:$password -o $org -h $url -e $env -p / -d `pwd`

echo "If 'State: deployed', then your API Proxy is ready to be invoked."

echo "Run 'invoke.sh'"
