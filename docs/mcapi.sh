#!/bin/bash
# Introspect yarn-mapped Minecraft 1.21.11 classes.
#   mcapi.sh sig <fqcn>      -> javap signatures
#   mcapi.sh find <pattern>  -> list matching class names
JAR="/c/Users/canel/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2/minecraft-merged-1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2.jar"
JAVAP="/c/Program Files/Microsoft/jdk-21.0.12.101-hotspot/bin/javap.exe"
JAR_TOOL="/c/Program Files/Microsoft/jdk-21.0.12.101-hotspot/bin/jar.exe"

case "$1" in
  sig)   shift; "$JAVAP" -cp "$JAR" "$@" ;;
  find)  shift; "$JAR_TOOL" tf "$JAR" | grep -Ei "$1" | sed 's|/|.|g; s|\.class$||' | sort | head -60 ;;
  *)     echo "usage: mcapi.sh sig <fqcn> | mcapi.sh find <pattern>" ;;
esac
