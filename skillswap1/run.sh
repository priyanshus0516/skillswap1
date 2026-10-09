#!/bin/bash
# SkillSwap Run Script

# Detect Java 11+
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
    JAVAC_CMD="$JAVA_HOME/bin/javac"
elif [ -x "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java" ]; then
    JAVA_CMD="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java"
    JAVAC_CMD="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/javac"
else
    JAVA_CMD="java"
    JAVAC_CMD="javac"
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Compile if out/ doesn't exist or if requested
if [ ! -d "out" ] || [ "$1" == "--build" ] || [ "$1" == "-b" ]; then
    echo "Compiling JPMS modules..."
    "$JAVAC_CMD" -d out --module-source-path modules $(find modules -name "*.java")
    if [ $? -ne 0 ]; then
        echo "Compilation failed!"
        exit 1
    fi
fi

# Run application
echo "Starting SkillSwap..."
exec "$JAVA_CMD" --module-path out:lib -m skillswap.gui/com.skillswap.gui.MainApp "$@"
