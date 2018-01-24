#!/bin/bash

gpb/bin/protoc-erl -o frontend/ negotiation-system/src/proto/*.proto
erlc -I gpb/include -I frontend/ -o frontend/ frontend/*.erl
