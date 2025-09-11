#!/bin/bash/


for file in $(find "./src/" -name "*.java"); do
    javac -d "./bin/" "$file"
done
