#!/bin/bash

for file in ./bin/*.class; do
    java -d "$file"
    if [ $? -ne 0 ]; then
        echo "execution failed for $file"
        exit 1
    fi
done