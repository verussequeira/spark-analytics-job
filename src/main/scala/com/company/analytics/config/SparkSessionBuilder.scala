package com.company.analytics.config

import org.apache.spark.sql.SparkSession
import com.typesafe.scalalogging.Logger

/**
 * WHAT THIS DOES:
 * - Creates and configures SparkSession
 * - Centralizes Spark configuration
 * - Similar to creating SparkConf and SparkSession in Java
 * 
 * JAVA EQUIVALENT:
 * public class SparkSessionBuilder {
 *   public static SparkSession build() { ... }
 * }
 */
object SparkSessionBuilder {
  
  // Logger instance (similar to slf4j Logger in Java)
  private val logger = Logger(getClass.getName)
  
  /**
   * Build SparkSession with configuration
   * 
   * PARAMETERS:
   * @param appName Optional application name (default from config)
   * @param master Optional master URL (default from config)
   * 
   * SCALA NOTE: Default parameters!
   * In Java you'd need method overloading:
   *   build()
   *   build(String appName)
   *   build(String appName, String master)
   * 
   * In Scala: just one method with defaults
   */
  def buildSession(
    appName: String = AppConfig.appName,
    master: String = AppConfig.sparkMaster
  ): SparkSession = {
    
    logger.info(s"Creating SparkSession: $appName")
    logger.info(s"Master: $master")
    
    // Builder pattern (same as Java Spark API)
    val spark = SparkSession
      .builder()
      .appName(appName)
      .master(master)
      // Set Spark configurations
      .config("spark.sql.shuffle.partitions", AppConfig.shufflePartitions)
      .config("spark.executor.memory", AppConfig.executorMemory)
      .config("spark.sql.adaptive.enabled", "true")
      // Enable Hive support if needed
      // .enableHiveSupport()
      .getOrCreate()
    
    // Propagate Spark config values to system properties for AspectJ to read
    // This allows Spark config to override application.conf
    // These can be passed as: --conf spark.logging.instrumentation.log-method-name=true
    propagateInstrumentationConfigs(spark)
    
    // Set log level to reduce noise
    spark.sparkContext.setLogLevel(AppConfig.loggingLevel)
    
    logger.info(s"SparkSession created successfully")
    logger.info(s"Spark Version: ${spark.version}")
    logger.info(s"Spark UI: ${spark.sparkContext.uiWebUrl.getOrElse("N/A")}")
    
    // Return the SparkSession
    // In Scala, last expression is automatically returned (no 'return' keyword needed)
    spark
  }
  
  /**
   * Propagate Spark configuration values to system properties
   * This allows AspectJ to read Spark config values
   */
  private def propagateInstrumentationConfigs(spark: SparkSession): Unit = {
    val sparkConf = spark.sparkContext.getConf
    
    // List of instrumentation config keys
    val configKeys = Seq(
      "spark.logging.instrumentation.log-method-name",
      "spark.logging.instrumentation.log-parameters",
      "spark.logging.instrumentation.log-duration"
    )
    
    configKeys.foreach { key =>
      sparkConf.getOption(key).foreach { value =>
        // Set as system property so AspectJ can read it
        System.setProperty(key, value)
        logger.debug(s"Propagated Spark config $key=$value to system property")
      }
    }
  }
  
  /**
   * Stop SparkSession gracefully
   * JAVA EQUIVALENT: public static void stopSession(SparkSession spark)
   */
  def stopSession(spark: SparkSession): Unit = {
    logger.info("Stopping SparkSession...")
    spark.stop()
    logger.info("SparkSession stopped")
  }
}