package com.company.analytics.jobs

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import com.company.analytics.readers.SimpleReader
import com.company.analytics.transformations.SimpleTransformations
import com.company.analytics.writers.SimpleWriter

/**
 * SIMPLE SALES ANALYTICS JOB
 * 
 * What this job does:
 * 1. Read sales data from CSV
 * 2. Clean the data (remove nulls, duplicates)
 * 3. Calculate total sales by region
 * 4. Write results to Parquet
 */
object SimpleSalesJob {
  
  def main(args: Array[String]): Unit = {
   //println("Starting Simple Sales Job")
    
    // Create Spark session
    val spark = SparkSession.builder()
      .appName("Simple Sales Job")
      .master("local[*]")  // Run locally
      .getOrCreate()
    
    try {
      // Run the job
      runJob(spark)
      
     //println("Job completed successfully!")
      
    } catch {
      case e: Exception =>
       //println(s"Job failed with error: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      // Always stop Spark at the end
      spark.stop()
    }
  }
  
  /**
   * Main job logic
   */
  def runJob(spark: SparkSession): Unit = {
    
    // STEP 1: Read data
   //println("\n=== STEP 1: Reading Data ===")
    val rawData = SimpleReader.readCsv(spark, "data/input/sales.csv")
    
    // Show first few rows
   //println("Raw data sample:")
    //rawData.show(5)
    
    // STEP 2: Clean data
   //println("\n=== STEP 2: Cleaning Data ===")
    
    // Remove nulls from important columns
    val withoutNulls = SimpleTransformations.removeNulls(
      rawData, 
      "order_id", 
      "customer_id", 
      "amount"
    )
    
    // Remove duplicate orders
    val cleaned = SimpleTransformations.removeDuplicatesBy(
      withoutNulls,
      "order_id"
    )
    
   //println("Cleaned data sample:")
    //cleaned.show(5)
    
    // STEP 3: Calculate total sales by region
   //println("\n=== STEP 3: Calculating Sales by Region ===")
    val salesByRegion = SimpleTransformations.groupAndSum(
      cleaned,
      "region",
      "amount"
    )
    
    // STEP 4: Calculate customer counts by region
   //println("\n=== STEP 4: Counting Customers by Region ===")
    val customerCounts = cleaned
      .select("region", "customer_id")
      .distinct()
      .groupBy("region")
      .count()
      .withColumnRenamed("count", "customer_count")
    
    //customerCounts.show()
    
    // STEP 5: Join the results
   //println("\n=== STEP 5: Combining Results ===")
    val finalResults = salesByRegion
      .join(customerCounts, "region")
      .orderBy(desc("total_amount"))
    
   //println("Final results:")
    //finalResults.show()
    
    // STEP 6: Write results
   //println("\n=== STEP 6: Writing Results ===")
    SimpleWriter.writeParquet(finalResults, "data/output/sales_summary.parquet")
    SimpleWriter.writeCsv(finalResults, "data/output/sales_summary.csv")
    
   //println("All done!")
  }
}