# Example: Using Spark Analytics Job as a Dependency

This guide walks you through using the Spark Analytics Job library in your project.

## Prerequisites

- Scala 2.12
- Spark 3.5.0
- SBT

## Step 1: Build the Library

If you have the source project:

```bash
cd spark-analytics-job
sbt assembly
```

This creates: `target/scala-2.12/spark-analytics-job-assembly-1.0.0.jar`

Copy this JAR to your project's dependency folder (e.g., `dependency/`).

## Step 2: Add to Your Project

Create or update your `build.sbt`:

```scala
name := "my-spark-project"
version := "1.0.0"
scalaVersion := "2.12.18"

libraryDependencies ++= Seq(
  // Spark dependencies
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  
  // Analytics job library (assembly JAR)
  "com.company.analytics" %% "spark-analytics-job" % "1.0.0" from 
    "file:///absolute/path/to/dependency/spark-analytics-job-assembly-1.0.0.jar",
  
  // Required dependencies
  "org.aspectj" % "aspectjweaver" % "1.9.19",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.11"
)

// Enable AspectJ weaver for load-time weaving
fork := true

run / javaOptions ++= {
  val weaverJar = (Runtime / fullClasspath).value
    .map(_.data)
    .find(_.getName.contains("aspectjweaver"))
    .getOrElse(throw new Exception("aspectjweaver.jar not found. Run 'sbt update' first."))
  Seq(s"-javaagent:${weaverJar.getAbsolutePath}")
}
```

## Step 3: Use in Your Code

Create a Scala file with your data processing logic:

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

## Step 4: Run Your Application

### Option A: Run with SBT

```bash
sbt run
```

### Option B: Run with Custom Configuration

```bash
sbt \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-method-name=true\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-parameters=false\"" \
  "set run / javaOptions += \"-Dspark.logging.instrumentation.log-duration=true\"" \
  run
```

### Option C: Run with Spark Submit

First, build your application JAR:
```bash
sbt package
```

Then submit:
```bash
spark-submit \
  --class com.yourcompany.YourMainClass \
  --conf spark.logging.instrumentation.log-method-name=true \
  --conf spark.logging.instrumentation.log-parameters=false \
  --conf spark.logging.instrumentation.log-duration=true \
  --conf spark.driver.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  --conf spark.executor.extraJavaOptions="-javaagent:/path/to/aspectjweaver-1.9.19.jar" \
  target/scala-2.12/my-spark-project_2.12-1.0.0.jar
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

### Logging doesn't appear?

1. **Check AspectJ weaver**: Look for `[AppClassLoader] info AspectJ Weaver Version` in logs
2. **Verify JAR is on classpath**: Check that the library JAR is included
3. **Check META-INF/aop.xml**: Should be in the JAR
4. **Verify annotations**: Make sure `@LogMethod` is on the methods you want to log

### Configuration doesn't work?

1. **System properties**: Must be passed to forked JVM (use `run / javaOptions`)
2. **Spark config**: Must be set before SparkSession is created
3. **Property names**: Must match exactly (case-sensitive)

### aspectjweaver not found?

1. Run `sbt update` to download dependencies
2. Verify `aspectjweaver` is in your `libraryDependencies`
3. Check that the JAR exists in your local repository
