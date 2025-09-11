#!/bin/bash
SRC_DIR="./bin/"
for file in $(find "$SRC_DIR" -name "*.class"); do
    java "$file"
done