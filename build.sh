#!/bin/bash
set -e

# This script is hard-coded for my personal development environment. Providing it on GitHub solely as reference for how builds were generated. You will need to set up your own build workflow. An LLM can likely adapt this file for you if you are not sure where to begin.
# -Christopher

# Path to GitHub forks
FORKS="/home/cwright/Projects/fun/minecraft"

# Dependency versions
FABRIC_LOADER="0.14.10/638450f4cfa4b1b25d6b05958284e5f86b638f38/fabric-loader-0.14.10"
SPONGE_MIXIN="0.17.0+mixin.0.8.7/cf31463202f72d03b0ef1e1e38e8ee71b7faaab6/sponge-mixin-0.17.0+mixin.0.8.7"
GSON="2.13.2/48b8230771e573b54ce6e867a9001e75977fe78e/gson-2.13.2"
JSPECIFY="1.0.0/7425a601c1c7ec76645a78d22b8c6a627edee507/jspecify-1.0.0"
BRIGADIER="1.3.10/d15b53a14cf20fdcaa98f731af5dda654452c010/brigadier-1.3.10"
FASTUTIL="8.5.18/a6cff377eecc19c2037bf31568a6d7106b50ba1f/fastutil-8.5.18"

# JAR output path
OUTPUT="."

# Compile Java files
echo "Compiling Java sources..."
rm -rf build/classes
mkdir -p build/classes

# Collect classpath
CP="$FORKS/Moonrise/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-2ffda7bfea/26.1.2/minecraft-merged-2ffda7bfea-26.1.2.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/net.fabricmc/fabric-loader/$FABRIC_LOADER.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/net.fabricmc/sponge-mixin/$SPONGE_MIXIN.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/$GSON.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/org.jspecify/jspecify/$JSPECIFY.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/com.mojang/brigadier/$BRIGADIER.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/$FASTUTIL.jar"
CP="$CP:libs/cloth-config-26.1.154.jar"
CP="$CP:libs/modmenu-18.0.0.jar"

JAVA_HOME="/home/cwright/.local/share/PandoraLauncher/runtime/graalvm-25.1.3/linux"
$JAVA_HOME/bin/javac --release 25 -cp "$CP" -d build/classes $(find src/main/java -name "*.java")

# Extract versions from gradle.properties
MC_VERSION=$(grep "^minecraft_version=" gradle.properties | cut -d'=' -f2)
MOD_VERSION=$(grep "^mod_version=" gradle.properties | cut -d'=' -f2)
JAR_NAME="player-dimmer-fabric-${MC_VERSION}-${MOD_VERSION}.jar"

echo "Packaging JAR..."
mkdir -p build/libs
cp -r src/main/resources/* build/classes/
cd build/classes
$JAVA_HOME/bin/jar cf ../libs/${JAR_NAME} .
cd ../..

echo "Done! The JAR is at build/libs/${JAR_NAME}"
cp build/libs/${JAR_NAME} ${OUTPUT}
echo "JAR also copied to \$OUTPUT path (\"$OUTPUT\")."
