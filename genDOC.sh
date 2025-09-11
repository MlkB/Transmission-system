#!/bin/bash 

for file in $(find "./src/" -name "*.java");do 
    javadoc -d ./docs/ $file 
done