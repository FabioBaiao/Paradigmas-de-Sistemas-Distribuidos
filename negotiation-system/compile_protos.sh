#!/bin/sh

protoc src/proto/clientSerializer.proto --java_out=src/main/java/
protoc src/proto/exchangeSerializer.proto --java_out=src/main/java
