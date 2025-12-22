# Example: Using Spark Analytics Job as a Dependency

## Step 1: Build the Library

```bash
cd spark-analytics-job
./scripts/build-library.sh
```

This creates: `target/scala-2.12/spark-analytics-job_2.12-1.0.0.jar`

## Step 2: Add to Your Project

### In your project's `build.sbt`:

```scala
name := "my-spark-project"
version := "1.0.0"
scalaVersion := "2.12.18"

libraryDependencies ++= Seq(
  // Spark dependencies
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  
  // Add the analytics job library
  "com.company.analytics" %% "spark-analytics-job" % "1.0.0" from 
    "file:///absolute/path/to/spark-analytics-job/target/scala-2.12/spark-analytics-job_2.12-1.0.0.jar"
)

// Enable AspectJ weaver
fork := true

run / javaOptions ++= {
  val weaverJar = (Runtime / fullClasspath).value
    .map(_.data)
    .find(_.getName.contains("aspectjweaver"))
    .getOrElse(throw new Exception("aspectjweaver.jar not found"))
  Seq(s"-javaagent:${weaverJar.getAbsolutePath}")
}
```

## Step 3: Use in Your Code

```scala
package com.yourcompany

import org.apache.spark.sql.{DataFrame, SparkSession}
import com.company.analytics.instrumentation.LogMethod

object MyDataProcessor {
  
  @LogMethod
  def processData(spark: SparkSession, inputPath: String): DataFrame = {
    spark.read.parquet(inputPath)
      .filter($"status" === "active")
      .select("id", "name", "value")
  }
  
  @LogMethod
  def aggregateData(df: DataFrame): DataFrame = {
    df.groupBy("category")
      .agg(sum("value").as("total"))
  }
}
```

## Step 4: Run with Logging Configuration

### Option A: Via System Properties

```bash
sbt \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-method-name=true\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-parameters=false\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-duration=true\"" \
  run
```

### Option B: Via Environment Variables

```bash
export SPARK_LOGGING_INSTRUMENTATION_LOG_METHOD_NAME=true
export SPARK_LOGGING_INSTRUMENTATION_LOG_PARAMETERS=false
export SPARK_LOGGING_INSTRUMENTATION_LOG_DURATION=true

sbt run
```

### Option C: Via Spark Submit

```bash
spark-submit \
  --class com.yourcompany.YourMainClass \
  --conf spark.logging.instrumentation.log-method-name=true \
  --conf spark.logging.instrumentation.log-parameters=false \
  --conf spark.logging.instrumentation.log-duration=true \
  --conf spark.driver.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  --conf spark.executor.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  your-app.jar
```

## Step 5: Verify It Works

When you run your application, you should see logs like:

```
[info] >>> STARTING: MyDataProcessor.processData
[info]     Parameters:
[info]       - spark = SparkSession...
[info]       - inputPath = /path/to/data
[info] --------------------------------------------------------------------------------
[info] <<< COMPLETED: MyDataProcessor.processData
[info]     Duration: 1234ms
[info]     Result: DataFrame[columns=3, partitions=10]
```

## Troubleshooting

### If logging doesn't appear:

1. **Check AspectJ weaver is enabled**: Look for messages like `[AppClassLoader] info AspectJ Weaver Version` in logs
2. **Verify JAR is on classpath**: Check that the library JAR is included
3. **Check META-INF/aop.xml**: Should be in the JAR
4. **Verify annotations**: Make sure `@LogMethod` is on the methods you want to log

### If configuration doesn't work:

1. **System properties**: Must be passed to forked JVM (use `run / javaOptions`)
2. **Spark config**: Must be set before SparkSession is created
3. **Property names**: Must match exactly (case-sensitive)

