#!/bin/bash
set -e

# Compile Java files
echo "Compiling Java sources..."
rm -rf build/classes
mkdir -p build/classes

# Collect classpath
CP="/home/cwright/Projects/sandbox/Moonrise/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-2ffda7bfea/26.1.2/minecraft-merged-2ffda7bfea-26.1.2.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/net.fabricmc/fabric-loader/0.14.10/638450f4cfa4b1b25d6b05958284e5f86b638f38/fabric-loader-0.14.10.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/net.fabricmc/sponge-mixin/0.17.0+mixin.0.8.7/cf31463202f72d03b0ef1e1e38e8ee71b7faaab6/sponge-mixin-0.17.0+mixin.0.8.7.jar"
CP="$CP:libs/cloth-config-26.1.154.jar"
CP="$CP:/home/cwright/Projects/sandbox/distant-horizons-vulkanmod/jars/modmenu-18.0.0.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.2.4/a60a5e993c98c864010053cb901b7eab25306568/gson-2.2.4.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/org.jspecify/jspecify/1.0.0/7425a601c1c7ec76645a78d22b8c6a627edee507/jspecify-1.0.0.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/com.mojang/brigadier/1.1.8/5244ce82c3337bba4a196a3ce858bfaecc74404a/brigadier-1.1.8.jar"
CP="$CP:/home/cwright/.gradle/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/8.5.9/bb7ea75ecdb216654237830b3a96d87ad91f8cc5/fastutil-8.5.9.jar"

# We also need Gson and SLF4J, they should be in the minecraft jar or fabric-loader jar.
# If they are not, we might need to find them. 
# Let's see if javac complains.

JAVA_HOME="/home/cwright/.local/share/PandoraLauncher/runtime/graalvm-25.1.3/linux"
$JAVA_HOME/bin/javac --release 25 -cp "$CP" -d build/classes $(find src/main/java -name "*.java")

echo "Packaging JAR..."
mkdir -p build/libs
cp -r src/main/resources/* build/classes/
cd build/classes
$JAVA_HOME/bin/jar cf ../libs/player-dimmer-1.0.0.jar .
cd ../..

echo "Done! The JAR is at build/libs/player-dimmer-1.0.0.jar"
cp build/libs/player-dimmer-1.0.0.jar .
echo "Copied to current directory."
