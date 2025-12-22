package com.company.analytics.readers

import org.apache.spark.sql.{DataFrame, SparkSession}
import com.company.analytics.instrumentation.LogMethod
/**
 * SIMPLE DATA READER
 * Just reads files - nothing fancy
 * 
 * Think of this like a utility class in Java with static methods
 */
object SimpleReader {
  
  /**
   * Read a Parquet file
   * 
   * USAGE: val df = SimpleReader.readParquet(spark, "data/input/sales.parquet")
   */
  def readParquet(spark: SparkSession, path: String): DataFrame = {
    println(s"Reading Parquet from: $path")
    
    // Simple read - same as Java Spark API
    val df = spark.read.parquet(path)
    
    println(s"Loaded ${df.count()} rows")
    df
  }
  
  /**
   * Read a CSV file with headers
   * 
   * USAGE: val df = SimpleReader.readCsv(spark, "data/input/customers.csv")
   */
  @LogMethod
  def readCsv(spark: SparkSession, path: String): DataFrame = {
    println(s"Reading CSV from: $path")
    
    // Read CSV with common options
    val df = spark.read
      .option("header", "true")       // First row is column names
      .option("inferSchema", "true")  // Auto-detect data types
      .csv(path)
    
    println(s"Loaded ${df.count()} rows with ${df.columns.length} columns")
    df
  }
  
  /**
   * Read from a database table
   * 
   * USAGE: 
   * val df = SimpleReader.readDatabase(
   *   spark, 
   *   "jdbc:mysql://localhost:3306/mydb",
   *   "customers",
   *   "username",
   *   "password"
   * )
   */
  def readDatabase(
    spark: SparkSession,
    jdbcUrl: String,
    tableName: String,
    user: String,
    password: String
  ): DataFrame = {
    println(s"Reading from database table: $tableName")
    
    // Read from database
    val df = spark.read
      .format("jdbc")
      .option("url", jdbcUrl)
      .option("dbtable", tableName)
      .option("user", user)
      .option("password", password)
      .load()
    
    println(s"Loaded ${df.count()} rows")
    df
  }
}