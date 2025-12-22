#!/bin/bash
# Helper script to run Spark job with instrumentation logging configuration
# Usage: ./scripts/run-with-logging.sh <job-name> [log-method-name] [log-parameters] [log-duration]
# Example: ./scripts/run-with-logging.sh sales true false true

JOB_NAME=${1:-sales}
LOG_METHOD_NAME=${2:-true}
LOG_PARAMETERS=${3:-true}
LOG_DURATION=${4:-true}

sbt \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-method-name=$LOG_METHOD_NAME\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-parameters=$LOG_PARAMETERS\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-duration=$LOG_DURATION\"" \
  "runMain com.company.analytics.SparkJobApp $JOB_NAME"

