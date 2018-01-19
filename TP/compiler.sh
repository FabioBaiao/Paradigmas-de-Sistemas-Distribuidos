#!/bin/bash

protoc --java_out=. *.proto
../gpb/bin/protoc-erl -o frontend/ *.proto
javac -cp ../protobuf-java-3.4.1.jar:../jeromq-0.4.3-SNAPSHOT.jar client/*.java exchange/*.java
erlc -I ../gpb/include -I frontend/ -o frontend/ frontend/*.erl
