# Spark Analytics Job - Library Usage Guide

This library provides AspectJ-based method logging instrumentation for Spark applications.

## Quick Start

### Step 1: Build the Library

Choose one of these options:

**Option A: Assembly JAR (Recommended when distributing just the JAR file)**
```bash
sbt assembly
```
Creates: `target/scala-2.12/spark-analytics-job-assembly-1.0.0.jar`

**Option B: Publish to Local Maven Repository (Recommended for development)**
```bash
sbt publishLocal
```
Publishes to: `~/.m2/repository/com/company/analytics/spark-analytics-job_2.12/1.0.0/`

**Option C: Standard Library JAR**
```bash
sbt package
```
Creates: `target/scala-2.12/spark-analytics-job_2.12-1.0.0.jar`

### Step 2: Add to Your Project

#### Using Assembly JAR (When you only have the JAR file)

In your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  
  // Assembly JAR (includes most dependencies)
  "com.company.analytics" %% "spark-analytics-job" % "1.0.0" from 
    "file:///path/to/spark-analytics-job-assembly-1.0.0.jar",
  
  // Required: AspectJ weaver must be separate for -javaagent
  "org.aspectj" % "aspectjweaver" % "1.9.19",
  
  // Required: Logging dependencies needed separately for classloader/service loader
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.11"
)

// Enable AspectJ weaver
fork := true

run / javaOptions ++= {
  val weaverJar = (Runtime / fullClasspath).value
    .map(_.data)
    .find(_.getName.contains("aspectjweaver"))
    .getOrElse(throw new Exception("aspectjweaver.jar not found. Run 'sbt update' first."))
  Seq(s"-javaagent:${weaverJar.getAbsolutePath}")
}
```

#### Using publishLocal (When you have access to the source project)

In your `build.sbt`:

```scala
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  
  // All dependencies (AspectJ, Config, Logging) are automatically included!
  "com.company.analytics" %% "spark-analytics-job" % "1.0.0"
)

// Enable AspectJ weaver
fork := true

run / javaOptions ++= {
  val weaverJar = (Runtime / fullClasspath).value
    .map(_.data)
    .find(_.getName.contains("aspectjweaver"))
    .getOrElse(throw new Exception("aspectjweaver.jar not found. Run 'sbt update' first."))
  Seq(s"-javaagent:${weaverJar.getAbsolutePath}")
}
```

### Step 3: Use in Your Code

```scala
import com.company.analytics.instrumentation.LogMethod

object MyService {
  @LogMethod
  def processData(df: DataFrame): DataFrame = {
    // Your code here
    df
  }
}
```

### Step 4: Configure Logging (Optional)

Control what gets logged via system properties, environment variables, or Spark configuration.

**System Properties:**
```bash
sbt "set run / javaOptions += \"-Dspark.logging.instrumentation.log-method-name=true\"" \
    "set run / javaOptions += \"-Dspark.logging.instrumentation.log-parameters=false\"" \
    "set run / javaOptions += \"-Dspark.logging.instrumentation.log-duration=true\"" \
    run
```

**Environment Variables:**
```bash
export SPARK_LOGGING_INSTRUMENTATION_LOG_METHOD_NAME=true
export SPARK_LOGGING_INSTRUMENTATION_LOG_PARAMETERS=false
export SPARK_LOGGING_INSTRUMENTATION_LOG_DURATION=true
sbt run
```

**Spark Configuration:**
```bash
spark-submit \
  --conf spark.logging.instrumentation.log-method-name=true \
  --conf spark.logging.instrumentation.log-parameters=false \
  --conf spark.logging.instrumentation.log-duration=true \
  --conf spark.driver.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  --conf spark.executor.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  your-app.jar
```

## Configuration Properties

- `spark.logging.instrumentation.log-method-name` (default: `true`) - Log method start/completion
- `spark.logging.instrumentation.log-parameters` (default: `true`) - Log method parameters
- `spark.logging.instrumentation.log-duration` (default: `true`) - Log execution duration

## Configuration Priority

1. System Properties (`-Dspark.logging.instrumentation.*`)
2. Environment Variables (`SPARK_LOGGING_INSTRUMENTATION_*`)
3. Spark Configuration (`--conf spark.logging.instrumentation.*`)
4. Default values from `application.conf`

## Troubleshooting

### AspectJ not working?
1. Ensure `-javaagent` is set correctly in `build.sbt`
2. Verify `aspectjweaver` dependency is included
3. Check that `META-INF/aop.xml` is in the JAR
4. Look for AspectJ weaver messages in logs

### Configuration not taking effect?
1. Check priority order (system properties override config)
2. Ensure properties are passed to the forked JVM (not just SBT)
3. Verify property names match exactly (case-sensitive)

## Dependencies

- Scala 2.12
- Spark 3.5.0 (provided by consumer)
- AspectJ 1.9.19
- Typesafe Config 1.4.2
- Scala Logging 3.9.5
- Logback Classic 1.4.11
