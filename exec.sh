#!/bin/bash

for file in $(find "./bin/" -name "*.class"); do
    java -cp ./bin *.Main
done