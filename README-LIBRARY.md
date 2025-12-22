# Spark Analytics Job - Library Usage Guide

This project can be packaged as a JAR and used as a dependency in other Scala/Spark projects. It provides AspectJ-based method logging instrumentation.

## Building the Library JAR

### Option 1: Standard Library JAR (Recommended for Maven/SBT dependencies)

```bash
sbt package
```

This creates: `target/scala-2.12/spark-analytics-job_2.12-1.0.0.jar`

### Option 2: Publish to Local Maven Repository

```bash
sbt publishLocal
```

This publishes to `~/.m2/repository/com/company/analytics/spark-analytics-job_2.12/1.0.0/`

### Option 3: Fat JAR (includes all dependencies except Spark)

```bash
sbt assembly
```

This creates: `target/scala-2.12/spark-analytics-job-assembly-1.0.0.jar`

## Using as a Dependency

### In SBT Project

Add to your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  // Your Spark dependencies
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  
  // Add this library
  "com.company.analytics" %% "spark-analytics-job" % "1.0.0" from "file:///path/to/spark-analytics-job/target/scala-2.12/spark-analytics-job_2.12-1.0.0.jar"
)
```

Or if published locally:

```scala
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  "com.company.analytics" %% "spark-analytics-job" % "1.0.0"
)
```

### In Maven Project

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.company.analytics</groupId>
    <artifactId>spark-analytics-job_2.12</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

## Using the Logging Instrumentation

### 1. Annotate Methods

In your project, annotate methods you want to log:

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

### 2. Configure Logging Aspects

You can control what gets logged via system properties, environment variables, or Spark configuration.

#### Via System Properties (Java/SBT)

```bash
java -Dspark.logging.instrumentation.log-method-name=true \
     -Dspark.logging.instrumentation.log-parameters=false \
     -Dspark.logging.instrumentation.log-duration=true \
     -javaagent:/path/to/aspectjweaver.jar \
     -cp your-app.jar YourMainClass
```

#### Via Spark Configuration

```bash
spark-submit \
  --conf spark.logging.instrumentation.log-method-name=true \
  --conf spark.logging.instrumentation.log-parameters=false \
  --conf spark.logging.instrumentation.log-duration=true \
  --conf spark.driver.extraJavaOptions="-javaagent:/path/to/aspectjweaver.jar" \
  --conf spark.executor.extraJavaOptions="-javaagent:/path/to/aspectjweaver.jar" \
  your-app.jar
```

#### Via Environment Variables

```bash
export SPARK_LOGGING_INSTRUMENTATION_LOG_METHOD_NAME=true
export SPARK_LOGGING_INSTRUMENTATION_LOG_PARAMETERS=false
export SPARK_LOGGING_INSTRUMENTATION_LOG_DURATION=true
```

### 3. Enable AspectJ Weaver

**IMPORTANT**: AspectJ requires the weaver agent to be loaded at JVM startup.

#### For SBT Projects

Add to your `build.sbt`:

```scala
fork := true

run / javaOptions ++= {
  val weaverJar = (Runtime / fullClasspath).value
    .map(_.data)
    .find(_.getName.contains("aspectjweaver"))
    .getOrElse(throw new Exception("aspectjweaver.jar not found"))
  Seq(s"-javaagent:${weaverJar.getAbsolutePath}")
}
```

#### For Spark Submit

```bash
spark-submit \
  --conf spark.driver.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  --conf spark.executor.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  your-app.jar
```

#### For Standalone Java Applications

```bash
java -javaagent:/path/to/aspectjweaver-1.9.19.jar \
     -cp your-app.jar:spark-analytics-job_2.12-1.0.0.jar \
     YourMainClass
```

## Configuration Priority

The logging configuration is read in this priority order:

1. **System Properties** (`-Dspark.logging.instrumentation.*`)
2. **Environment Variables** (`SPARK_LOGGING_INSTRUMENTATION_*`)
3. **Spark Configuration** (`--conf spark.logging.instrumentation.*`)
4. **application.conf** (default values)

## Configuration Properties

- `spark.logging.instrumentation.log-method-name` (default: `true`)
  - Logs method start/completion messages
  
- `spark.logging.instrumentation.log-parameters` (default: `true`)
  - Logs method parameters
  
- `spark.logging.instrumentation.log-duration` (default: `true`)
  - Logs method execution duration

## Example Usage

### Example 1: SBT Project

```scala
// build.sbt
libraryDependencies += "com.company.analytics" %% "spark-analytics-job" % "1.0.0"

// Your code
import com.company.analytics.instrumentation.LogMethod

object DataProcessor {
  @LogMethod
  def transform(df: DataFrame): DataFrame = {
    df.filter($"status" === "active")
  }
}
```

Run with:
```bash
sbt "set run / javaOptions += \"-Dspark.logging.instrumentation.log-duration=false\"" run
```

### Example 2: Spark Submit

```bash
spark-submit \
  --class com.yourcompany.YourApp \
  --conf spark.logging.instrumentation.log-method-name=true \
  --conf spark.logging.instrumentation.log-parameters=false \
  --conf spark.logging.instrumentation.log-duration=true \
  --conf spark.driver.extraJavaOptions="-javaagent:aspectjweaver-1.9.19.jar" \
  your-app.jar
```

## Troubleshooting

### AspectJ not working?

1. Ensure `-javaagent` is set correctly
2. Check that `META-INF/aop.xml` is in the JAR
3. Verify AspectJ dependencies are on classpath
4. Check logs for AspectJ weaver messages

### Configuration not taking effect?

1. Check priority order (system properties override config)
2. Ensure properties are passed to the forked JVM (not just SBT)
3. Verify property names match exactly

## Dependencies

This library requires:
- Scala 2.12
- Spark 3.5.0 (provided by consumer)
- AspectJ 1.9.19 (included in JAR)
- Typesafe Config 1.4.2 (included in JAR)
- Scala Logging 3.9.5 (included in JAR)

