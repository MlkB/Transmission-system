#!/bin/bash
cd ~/Desktop/TP-systemes-transm/Transmission-system/bin/
for file in ~/Desktop/TP-systemes-transm/Transmission-system/src/**/; do
    javac -d "$file"
    if [ $? -ne 0 ]; then
        echo "Compilation failed for $file"
        exit 1
    fi
done