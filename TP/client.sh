#!/bin/bash

# ./client.sh HOST PORT ZMQHOST ZMQPORT
java -cp ../protobuf-java-3.4.1.jar:../jeromq-0.4.3-SNAPSHOT.jar:. client.Client $*
