name := "spark-analytics-job"
version := "1.0.0"
scalaVersion := "2.12.18"
organization := "com.company.analytics"

// Spark dependencies - marked as "provided" for library usage
// Consumer projects should provide Spark dependencies
libraryDependencies ++= Seq(
   "org.apache.spark" %% "spark-core" % "3.5.0" % "provided",
   "org.apache.spark" %% "spark-sql" % "3.5.0" % "provided",

  // AspectJ dependencies - included in JAR (required for runtime)
  "org.aspectj" % "aspectjrt" % "1.9.19",
  "org.aspectj" % "aspectjweaver" % "1.9.19",

  // Configuration - included in JAR
  "com.typesafe" % "config" % "1.4.2",
  
  // Logging - included in JAR
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.11",
  
  // Testing
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)

// For running locally with 'sbt run'
fork := true

// AspectJ weaver agent - required for load-time weaving
// Dynamically find aspectjweaver.jar from the classpath at runtime
run / javaOptions ++= {
  val weaverJar = (Runtime / fullClasspath).value
    .map(_.data)
    .find(_.getName.contains("aspectjweaver"))
    .getOrElse(throw new Exception("aspectjweaver.jar not found in classpath. Run 'sbt update' first."))
  Seq(s"-javaagent:${weaverJar.getAbsolutePath}")
}

// Increase memory for SBT and fix Java module access for Spark
javaOptions ++= Seq(
  "-Xmx4G",
  "-XX:+UseG1GC",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
  "--add-opens=java.base/java.io=ALL-UNNAMED",
  "--add-opens=java.base/java.net=ALL-UNNAMED",
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
  "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
  "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED"
)

// Publishing settings for library usage
publishMavenStyle := true
publishTo := Some(Resolver.file("local", file(Path.userHome.absolutePath + "/.m2/repository")))

// Assembly settings (for building fat JAR for cluster deployment)
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "aop.xml") => MergeStrategy.first  // Keep AspectJ aop.xml
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}

// When building for cluster, exclude Spark (it will be provided by cluster)
assembly / assemblyExcludedJars := {
  val cp = (assembly / fullClasspath).value
  cp filter { f =>
    f.data.getName.contains("spark-")
  }
}

// Resources (including META-INF/aop.xml) are automatically included by SBT
// No need to manually add them