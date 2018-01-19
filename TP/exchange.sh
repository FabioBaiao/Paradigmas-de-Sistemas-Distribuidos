#!/bin/bash

java -cp ../protobuf-java-3.4.1.jar:../jeromq-0.4.3-SNAPSHOT.jar:. exchange/ExchangeServer $*
