#!/bin/bash
set -e

IMAGE="scrollshield-build"
APK_SRC="/app/app/build/outputs/apk/debug/app-debug.apk"
APK_DST="./ScrollShield.apk"

echo "Building Docker image..."
docker build -t $IMAGE .

echo "Extracting APK..."
CONTAINER=$(docker create $IMAGE)
docker cp "$CONTAINER:$APK_SRC" "$APK_DST"
docker rm "$CONTAINER" > /dev/null

echo "Done: $APK_DST"
