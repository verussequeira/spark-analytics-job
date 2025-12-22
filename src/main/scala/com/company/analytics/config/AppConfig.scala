package com.company.analytics.config

import com.typesafe.config.{Config, ConfigFactory}

/**
 * WHAT THIS DOES:
 * - Loads configuration from application.conf file
 * - Similar to Spring's @Configuration or Properties in Java
 * - Provides type-safe access to config values
 * 
 * JAVA EQUIVALENT:
 * Like having a Configuration class that reads from application.properties
 */
object AppConfig {
  // 'object' keyword = singleton (like static class in Java)
  // No need to instantiate: just call AppConfig.config
  
  // Load configuration from resources/application.conf
  // 'lazy val' = computed once on first access (like lazy initialization)
  private lazy val config: Config = ConfigFactory.load()
  
  // Application settings
  // 'def' = method (like Java method, but return type comes after :)
  def appName: String = config.getString("app.name")
  
  // Input settings
  def inputBasePath: String = config.getString("app.input.base-path")
  def inputFormat: String = config.getString("app.input.format")
  
  // Output settings
  def outputBasePath: String = config.getString("app.output.base-path")
  def outputFormat: String = config.getString("app.output.format")
  def outputMode: String = config.getString("app.output.mode")
  
  // Spark settings
  def sparkMaster: String = config.getString("spark.master")
  def shufflePartitions: Int = config.getInt("spark.sql.shuffle.partitions")
  def executorMemory: String = config.getString("spark.executor.memory")
  
  // Logging settings
  def loggingLevel: String = config.getString("logging.level")
  def instrumentationEnabled: Boolean = config.getBoolean("logging.instrumentation")
  
  // Instrumentation logging aspects (can be overridden via Spark config or system properties)
  // Priority: System property > Environment variable > Spark config property > application.conf
  def logMethodName: Boolean = {
    getBooleanConfig(
      systemProp = "spark.logging.instrumentation.log-method-name",
      envVar = "SPARK_LOGGING_INSTRUMENTATION_LOG_METHOD_NAME",
      sparkConfig = "spark.logging.instrumentation.log-method-name",
      default = config.getBoolean("logging.instrumentation.log-method-name")
    )
  }
  
  def logParameters: Boolean = {
    getBooleanConfig(
      systemProp = "spark.logging.instrumentation.log-parameters",
      envVar = "SPARK_LOGGING_INSTRUMENTATION_LOG_PARAMETERS",
      sparkConfig = "spark.logging.instrumentation.log-parameters",
      default = config.getBoolean("logging.instrumentation.log-parameters")
    )
  }
  
  def logDuration: Boolean = {
    getBooleanConfig(
      systemProp = "spark.logging.instrumentation.log-duration",
      envVar = "SPARK_LOGGING_INSTRUMENTATION_LOG_DURATION",
      sparkConfig = "spark.logging.instrumentation.log-duration",
      default = config.getBoolean("logging.instrumentation.log-duration")
    )
  }
  
  /**
   * Helper method to get boolean config with priority:
   * 1. System property
   * 2. Environment variable
   * 3. Spark config (via system property set by Spark)
   * 4. Default value
   */
  private def getBooleanConfig(
    systemProp: String,
    envVar: String,
    sparkConfig: String,
    default: Boolean
  ): Boolean = {
    Option(System.getProperty(systemProp))
      .orElse(Option(System.getenv(envVar)))
      .orElse(Option(System.getProperty(sparkConfig)))
      .map(_.toBoolean)
      .getOrElse(default)
  }
  
  /**
   * Print all configuration (useful for debugging)
   * JAVA EQUIVALENT: public void printConfig() { ... }
   */
  def printConfig(): Unit = { // Unit = void in Java
    println(s"App Name: $appName")           // s"..." = string interpolation (like String.format)
    println(s"Input Path: $inputBasePath")
    println(s"Output Path: $outputBasePath")
    println(s"Spark Master: $sparkMaster")
  }
}