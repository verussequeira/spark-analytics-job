#!/bin/bash
# Script to build the library JAR for use as a dependency

set -e

echo "Building Spark Analytics Job Library..."
echo ""

# Build standard library JAR
echo "1. Building standard library JAR..."
sbt clean package

JAR_PATH="target/scala-2.12/spark-analytics-job_2.12-1.0.0.jar"

if [ -f "$JAR_PATH" ]; then
    echo "✓ Library JAR created: $JAR_PATH"
    echo ""
    echo "To use as a dependency:"
    echo "  - Add to SBT: \"com.company.analytics\" %% \"spark-analytics-job\" % \"1.0.0\" from \"file://$(pwd)/$JAR_PATH\""
    echo "  - Or publish locally: sbt publishLocal"
else
    echo "✗ Failed to create JAR"
    exit 1
fi

echo ""
echo "2. Building fat JAR (optional, for standalone use)..."
sbt assembly

ASSEMBLY_PATH="target/scala-2.12/spark-analytics-job-assembly-1.0.0.jar"

if [ -f "$ASSEMBLY_PATH" ]; then
    echo "✓ Fat JAR created: $ASSEMBLY_PATH"
    echo "  (Includes all dependencies except Spark)"
else
    echo "✗ Failed to create assembly JAR"
    exit 1
fi

echo ""
echo "Build complete!"
echo ""
echo "Next steps:"
echo "  1. Use the library JAR as a dependency in your project"
echo "  2. Ensure AspectJ weaver is enabled (see README-LIBRARY.md)"
echo "  3. Configure logging aspects via system properties or Spark config"

