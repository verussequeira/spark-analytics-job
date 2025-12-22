package com.company.analytics.transformations

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import com.company.analytics.instrumentation.LogMethod
/**
 * SIMPLE TRANSFORMATIONS
 * Common data operations made simple
 * 
 * All methods take a DataFrame and return a transformed DataFrame
 */
object SimpleTransformations {
  
  /**
   * Remove null values from important columns
   * 
   * USAGE: val cleaned = SimpleTransformations.removeNulls(df, "customer_id", "amount")
   */
  @LogMethod
  def removeNulls(df: DataFrame, columns: String*): DataFrame = {
    //println(s"Removing nulls from columns: ${columns.mkString(", ")}")
    
    // Start with original DataFrame
    var result = df
    
    // Remove nulls from each column
    // In Java: for (String col : columns) { result = result.filter(... ) }
    for (col <- columns) {
      result = result.filter(result(col).isNotNull)
    }
    
    ////println(s"Rows after removing nulls: ${result.count()}")
    result
  }
  
  /**
   * Remove duplicate rows
   * 
   * USAGE: val deduplicated = SimpleTransformations.removeDuplicates(df)
   */
  @LogMethod
  def removeDuplicates(df: DataFrame): DataFrame = {
    //println("Removing duplicate rows")
    
    val result = df.dropDuplicates()
    
    ////println(s"Rows after removing duplicates: ${result.count()}")
    result
  }
  
  /**
   * Remove duplicates based on specific columns
   * 
   * USAGE: val deduplicated = SimpleTransformations.removeDuplicatesBy(df, "customer_id", "order_date")
   */
  def removeDuplicatesBy(df: DataFrame, columns: String*): DataFrame = {
    //println(s"Removing duplicates based on: ${columns.mkString(", ")}")
    
    val result = df.dropDuplicates(columns)
    
    ////println(s"Rows after deduplication: ${result.count()}")
    result
  }
  
  /**
   * Add a new column based on calculation
   * 
   * EXAMPLE: Add a "total" column by multiplying quantity * price
   * val withTotal = SimpleTransformations.addCalculatedColumn(
   *   df, 
   *   "total", 
   *   col("quantity") * col("price")
   * )
   */
  def addCalculatedColumn(df: DataFrame, newColumnName: String, calculation: org.apache.spark.sql.Column): DataFrame = {
    //println(s"Adding new column: $newColumnName")
    
    val result = df.withColumn(newColumnName, calculation)
    
    result
  }
  
  /**
   * Filter rows based on a condition
   * 
   * EXAMPLE: Keep only rows where amount > 100
   * val filtered = SimpleTransformations.filterData(df, col("amount") > 100)
   */
  def filterData(df: DataFrame, condition: org.apache.spark.sql.Column): DataFrame = {
    //println(s"Filtering data with condition")
    
    val result = df.filter(condition)
    
    //println(s"Rows after filtering: ${result.count()}")
    result
  }
  
  /**
   * Group by columns and aggregate
   * 
   * EXAMPLE: Total sales by region
   * val summary = SimpleTransformations.groupAndSum(df, "region", "sales_amount")
   */
  def groupAndSum(df: DataFrame, groupByColumn: String, sumColumn: String): DataFrame = {
    //println(s"Grouping by $groupByColumn and summing $sumColumn")
    
    val result = df.groupBy(groupByColumn)
      .agg(sum(sumColumn).alias(s"total_${sumColumn}"))
    
    //result.show()
    result
  }
  
  /**
   * Group by and count
   * 
   * EXAMPLE: Count orders per customer
   * val counts = SimpleTransformations.groupAndCount(df, "customer_id")
   */
  def groupAndCount(df: DataFrame, groupByColumn: String): DataFrame = {
    //println(s"Counting rows by $groupByColumn")
    
    val result = df.groupBy(groupByColumn)
      .count()
      .orderBy(desc("count"))  // Sort by count descending
    
    //result.show()
    result
  }
  
  /**
   * Select only specific columns
   * 
   * USAGE: val subset = SimpleTransformations.selectColumns(df, "customer_id", "name", "email")
   */
  def selectColumns(df: DataFrame, columns: String*): DataFrame = {
    //println(s"Selecting columns: ${columns.mkString(", ")}")
    
    df.select(columns.map(col): _*)
  }
  
  /**
   * Rename a column
   * 
   * USAGE: val renamed = SimpleTransformations.renameColumn(df, "old_name", "new_name")
   */
  def renameColumn(df: DataFrame, oldName: String, newName: String): DataFrame = {
    //println(s"Renaming column: $oldName -> $newName")
    
    df.withColumnRenamed(oldName, newName)
  }
}