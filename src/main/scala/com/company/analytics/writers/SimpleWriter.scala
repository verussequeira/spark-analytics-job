package com.company.analytics.writers

import org.apache.spark.sql.{DataFrame, SaveMode}

/**
 * SIMPLE DATA WRITER
 * Write DataFrames to different formats
 */
object SimpleWriter {
  
  /**
   * Write DataFrame as Parquet
   * 
   * USAGE: SimpleWriter.writeParquet(df, "data/output/results.parquet")
   */
  def writeParquet(df: DataFrame, path: String): Unit = {
    //println(s"Writing Parquet to: $path")
    
    df.write
      .mode(SaveMode.Overwrite)  // Overwrite if exists
      .parquet(path)
    
    //println("Write completed")
  }
  
  /**
   * Write DataFrame as CSV with headers
   * 
   * USAGE: SimpleWriter.writeCsv(df, "data/output/results.csv")
   */
  def writeCsv(df: DataFrame, path: String): Unit = {
    //println(s"Writing CSV to: $path")
    
    df.write
      .mode(SaveMode.Overwrite)
      .option("header", "true")
      .csv(path)
    
    //println("Write completed")
  }
  
  /**
   * Write to database table
   * 
   * USAGE: 
   * SimpleWriter.writeToDatabase(
   *   df,
   *   "jdbc:mysql://localhost:3306/mydb",
   *   "results_table",
   *   "username",
   *   "password"
   * )
   */
  def writeToDatabase(
    df: DataFrame,
    jdbcUrl: String,
    tableName: String,
    user: String,
    password: String
  ): Unit = {
    //println(s"Writing to database table: $tableName")
    
    df.write
      .format("jdbc")
      .option("url", jdbcUrl)
      .option("dbtable", tableName)
      .option("user", user)
      .option("password", password)
      .mode(SaveMode.Overwrite)
      .save()
    
    //println("Write completed")
  }
  
  /**
   * Write partitioned data (useful for big data)
   * Splits data into folders based on column values
   * 
   * EXAMPLE: Partition by year and month
   * SimpleWriter.writePartitioned(df, "data/output", "year", "month")
   */
  def writePartitioned(df: DataFrame, path: String, partitionColumns: String*): Unit = {
    //println(s"Writing partitioned data to: $path")
    //println(s"Partition columns: ${partitionColumns.mkString(", ")}")
    
    df.write
      .mode(SaveMode.Overwrite)
      .partitionBy(partitionColumns: _*)
      .parquet(path)
    
    //println("Write completed")
  }
}