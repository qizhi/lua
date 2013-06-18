#!/bin/bash

# Creates test users via the REST API.

# number of users to create
NUMBER_OF_USERS=100

# user service base url
USER_SERVICE_URL=http://localhost:9090/user-service-rest/rest

# base user name, a sequence number will be appended to it
BASE_USER_NAME="Bot_"

# base password, a sequence number will be appended to it
BASE_PWD=""

# operator id
OPERATOR_ID=0

for ((i = 0; i < $NUMBER_OF_USERS; i++)) do
   data='{"user" : { "userName" : "'$BASE_USER_NAME$i'", "operatorId" : "'$OPERATOR_ID'",
       "userInformation" : { "city" : "Stockholm", "country" : "Sweden" }}, "password" : "'$BASE_PWD$i'"}'
   echo curl -XPOST -H "Content-type: application/json" $USER_SERVICE_URL/users -d "$data"
done

