package com.company.analytics

import org.apache.spark.sql.SparkSession
import com.company.analytics.jobs.SimpleSalesJob

/**
 * MAIN APPLICATION
 * Entry point for running jobs
 */
object SparkJobApp {
  
  def main(args: Array[String]): Unit = {
    
    // Check which job to run
    if (args.length == 0) {
      //println("Usage: SparkJobApp <job-name>")
      //println("Available jobs:")
      //println("  - sales    : Run sales analytics job")
      System.exit(1)
    }
    
    val jobName = args(0)
    
    //println(s"Running job: $jobName")
    
    // Run the appropriate job
    jobName match {
      case "sales" =>
        SimpleSalesJob.main(args.drop(1))  // Pass remaining arguments
        
      case _ =>
        //println(s"Unknown job: $jobName")
        System.exit(1)
    }
  }
}