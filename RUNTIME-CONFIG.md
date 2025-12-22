# Runtime Configuration Reference

Quick reference for configuring logging aspects at runtime.

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `spark.logging.instrumentation.log-method-name` | `true` | Log method start/completion |
| `spark.logging.instrumentation.log-parameters` | `true` | Log method parameters |
| `spark.logging.instrumentation.log-duration` | `true` | Log execution duration |

## Quick Commands

### Disable Duration Only
```bash
sbt "set run / javaOptions += \"-Dspark.logging.instrumentation.log-duration=false\"" run
```

### Only Method Name (no params, no duration)
```bash
sbt \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-parameters=false\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-duration=false\"" \
  run
```

### Only Duration
```bash
sbt \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-method-name=false\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-parameters=false\"" \
  run
```

### Spark Submit Example
```bash
spark-submit \
  --conf spark.logging.instrumentation.log-method-name=true \
  --conf spark.logging.instrumentation.log-parameters=false \
  --conf spark.logging.instrumentation.log-duration=true \
  --conf spark.driver.extraJavaOptions="-javaagent:aspectjweaver-1.9.19.jar" \
  your-app.jar
```

## Priority Order

1. System Properties (`-D`)
2. Environment Variables
3. Spark Configuration (`--conf`)
4. `application.conf` (default)

