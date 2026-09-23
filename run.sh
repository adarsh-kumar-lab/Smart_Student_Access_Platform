#!/bin/bash
# Runs the app. Compiles first automatically if "out" folder doesn't exist yet.

if [ ! -d "out" ]; then
    echo "No build found, compiling first..."
    ./compile.sh
fi

java -cp out Main
