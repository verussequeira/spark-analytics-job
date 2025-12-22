package com.company.analytics.instrumentation

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import com.typesafe.scalalogging.Logger
import com.company.analytics.config.AppConfig

/**
 * ASPECTJ ASPECT
 * This intercepts methods annotated with @LogMethod
 * and prints start/end automatically
 * 
 * HOW IT WORKS:
 * 1. @Aspect tells AspectJ this is an aspect
 * 2. @Around intercepts the method call
 * 3. We print before and after the actual method runs
 */
@Aspect
class MethodLoggingAspect {
  
  // Logger instance for method logging
  private val logger = Logger(getClass.getName)
  
  /**
   * Intercept all methods with @LogMethod annotation
   * 
   * EXPLANATION:
   * - @Around = Run code before AND after the method
   * - execution(@...LogMethod * *(..)) = Match methods with @LogMethod
   * - ProceedingJoinPoint = The actual method being intercepted
   */
  @Around("execution(@com.company.analytics.instrumentation.LogMethod * *(..))")
  def logMethod(joinPoint: ProceedingJoinPoint): Object = {
    
    // Get method information
    val signature = joinPoint.getSignature.asInstanceOf[MethodSignature]
    val className = signature.getDeclaringTypeName.split("\\.").last
    val methodName = signature.getName
    
    // Get method parameters
    val args = joinPoint.getArgs
    val paramNames = signature.getParameterNames
    
    // Check configuration for what to log
    val shouldLogMethodName = AppConfig.logMethodName
    val shouldLogParameters = AppConfig.logParameters
    val shouldLogDuration = AppConfig.logDuration
    
    // LOG: Method start (only if method name logging is enabled)
    if (shouldLogMethodName) {
      logger.info("=" * 80)
      logger.info(s">>> STARTING: $className.$methodName")
    }
    
    // Log parameters if enabled and parameters exist
    if (shouldLogParameters && args.nonEmpty) {
      logger.info(s"    Parameters:")
      paramNames.zip(args).foreach { case (name, value) =>
        logger.info(s"      - $name = ${formatValue(value)}")
      }
    }
    
    if (shouldLogMethodName) {
      logger.info("-" * 80)
    }
    
    // Record start time
    val startTime = System.currentTimeMillis()
    
    // EXECUTE THE ACTUAL METHOD
    // This is where the real method runs
    var result: Object = null
    try {
      result = joinPoint.proceed()
      
      // Calculate duration
      val duration = System.currentTimeMillis() - startTime
      
      // LOG: Method end (success) - check config for what to log
      val shouldLogMethodName = AppConfig.logMethodName
      val shouldLogDuration = AppConfig.logDuration
      
      if (shouldLogMethodName || shouldLogDuration) {
        logger.info("-" * 80)
      }
      
      if (shouldLogMethodName) {
        logger.info(s"<<< COMPLETED: $className.$methodName")
      }
      
      if (shouldLogDuration) {
        logger.info(s"    Duration: ${duration}ms")
      }
      
      if (shouldLogMethodName) {
        logger.info(s"    Result: ${formatValue(result)}")
        logger.info("=" * 80)
        logger.info("")
      } else if (shouldLogDuration) {
        logger.info("")
      }
      
      result
      
    } catch {
      case e: Exception =>
        // Calculate duration even on error
        val duration = System.currentTimeMillis() - startTime
        
        // LOG: Method end (error) - check config for what to log
        val shouldLogMethodName = AppConfig.logMethodName
        val shouldLogDuration = AppConfig.logDuration
        
        if (shouldLogMethodName || shouldLogDuration) {
          logger.info("-" * 80)
        }
        
        if (shouldLogMethodName) {
          logger.info(s"!!! FAILED: $className.$methodName")
        }
        
        if (shouldLogDuration) {
          logger.info(s"    Duration: ${duration}ms")
        }
        
        if (shouldLogMethodName) {
          logger.info(s"    Error: ${e.getMessage}")
          logger.info("=" * 80)
          logger.info("")
        } else if (shouldLogDuration) {
          logger.info("")
        }
        
        throw e
    }
  }
  
  /**
   * Format values for clean printing
   * Handles special cases like DataFrames
   */
  private def formatValue(value: Any): String = {
    if (value == null) {
      "null"
    } else {
      value match {
        // Handle Spark DataFrame (match on Dataset[_] to avoid type erasure warning)
        // DataFrame is Dataset[Row], so we match Dataset[_] and check for columns method
        case df: org.apache.spark.sql.Dataset[_] =>
          try {
            // Try to access columns - only DataFrames have this method
            val dataframe = df.asInstanceOf[org.apache.spark.sql.DataFrame]
            s"DataFrame[columns=${dataframe.columns.length}, partitions=${dataframe.rdd.getNumPartitions}]"
          } catch {
            case _: Exception => 
              // Not a DataFrame, fall back to generic Dataset representation
              s"Dataset[partitions=${df.rdd.getNumPartitions}]"
          }
        
        // Handle Spark RDD
        case rdd: org.apache.spark.rdd.RDD[_] => 
          s"RDD[partitions=${rdd.getNumPartitions}]"
        
        // Handle collections (truncate if large)
        case seq: Seq[_] if seq.length > 3 => 
          s"Seq(${seq.take(3).mkString(", ")}, ... ${seq.length} items)"
        
        case arr: Array[_] if arr.length > 3 => 
          s"Array(${arr.take(3).mkString(", ")}, ... ${arr.length} items)"
        
        // Handle strings (truncate if very long)
        case s: String if s.length > 100 => 
          s.take(100) + "..."
        
        // Everything else: use toString
        case other => 
          other.toString
      }
    }
  }
}