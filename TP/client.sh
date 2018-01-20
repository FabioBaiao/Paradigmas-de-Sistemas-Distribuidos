#!/bin/bash

# ./client.sh HOST PORT ZMQHOST ZMQPORT
java -cp ../protobuf-java-3.4.1.jar:. client.Client $*
