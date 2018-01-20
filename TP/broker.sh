#!/bin/bash

# ./broker EXCHANGESPORT CLIENTSPORT
java -cp ../jeromq-0.4.3-SNAPSHOT.jar:. Broker $*
