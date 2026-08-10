#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./bump-version.sh <new_version>"
    echo "Example: ./bump-version.sh 1.0.2"
    exit 1
fi

NEW_VERSION=$1

# Update gradle.properties
sed -i "s/^mod_version=.*/mod_version=$NEW_VERSION/" gradle.properties

# Update fabric.mod.json
sed -i -E "s/\"version\": \"[^\"]+\"/\"version\": \"$NEW_VERSION\"/" src/main/resources/fabric.mod.json

echo "Version bumped to $NEW_VERSION in gradle.properties and fabric.mod.json"
