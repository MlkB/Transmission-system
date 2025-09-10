#!/bin/bash

for file in ./src/**/*.java; do
    javac -d ./bin "$file"
    if [ $? -ne 0 ]; then
        echo "Compilation failed for $file"
        exit 1
    fi
done