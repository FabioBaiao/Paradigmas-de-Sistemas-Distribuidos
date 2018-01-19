#!/bin/bash

# ./client.sh HOST PORT
java -cp ../protobuf-java-3.4.1.jar:. client.Client $*
