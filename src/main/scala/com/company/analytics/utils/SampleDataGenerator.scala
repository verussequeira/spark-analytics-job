package com.company.analytics.utils

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import java.io.PrintWriter

/**
 * GENERATE SAMPLE DATA
 * Creates test CSV files for development
 */
object SampleDataGenerator {
  
  def main(args: Array[String]): Unit = {
    // Create sample sales CSV
    generateSampleSalesCsv()
    println("Sample data generated in data/input/")
  }
  
  /**
   * Generate sample sales.csv file
   */
  def generateSampleSalesCsv(): Unit = {
    val writer = new PrintWriter("data/input/sales.csv")
    
    // Write header
    writer.println("order_id,customer_id,region,amount,product")
    
    // Write sample rows
    writer.println("ORD-001,CUST-101,North,150.50,Laptop")
    writer.println("ORD-002,CUST-102,South,89.99,Mouse")
    writer.println("ORD-003,CUST-103,East,1200.00,Monitor")
    writer.println("ORD-004,CUST-101,North,45.00,Keyboard")
    writer.println("ORD-005,CUST-104,West,299.99,Webcam")
    writer.println("ORD-006,CUST-102,South,599.00,Tablet")
    writer.println("ORD-007,CUST-105,North,75.50,Cable")
    writer.println("ORD-008,CUST-103,East,1500.00,Laptop")
    writer.println("ORD-009,CUST-106,West,199.99,Headphones")
    writer.println("ORD-010,CUST-101,North,49.99,Mouse")
    
    writer.close()
    println("Created: data/input/sales.csv")
  }
}