error id: file://<WORKSPACE>/src/main/scala/com/company/analytics/instrumentation/MethodLoggingAspect.scala:
file://<WORKSPACE>/src/main/scala/com/company/analytics/instrumentation/MethodLoggingAspect.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -org/aspectj/lang/annotation/formatValue.
	 -org/aspectj/lang/annotation/formatValue#
	 -org/aspectj/lang/annotation/formatValue().
	 -formatValue.
	 -formatValue#
	 -formatValue().
	 -scala/Predef.formatValue.
	 -scala/Predef.formatValue#
	 -scala/Predef.formatValue().
offset: 1558
uri: file://<WORKSPACE>/src/main/scala/com/company/analytics/instrumentation/MethodLoggingAspect.scala
text:
```scala
package com.company.analytics.instrumentation

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation._
import org.aspectj.lang.reflect.MethodSignature

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
    
    // PRINT: Method start
    println("=" * 80)
    println(s">>> STARTING: $className.$methodName")
    
    // Print parameters if any
    if (args.nonEmpty) {
      println(s"    Parameters:")
      paramNames.zip(args).foreach { case (name, value) =>
        println(s"      - $name = ${formatValue@@(value)}")
      }
    }
    
    println("-" * 80)
    
    // Record start time
    val startTime = System.currentTimeMillis()
    
    // EXECUTE THE ACTUAL METHOD
    // This is where the real method runs
    var result: Object = null
    try {
      result = joinPoint.proceed()
      
      // Calculate duration
      val duration = System.currentTimeMillis() - startTime
      
      // PRINT: Method end (success)
      println("-" * 80)
      println(s"<<< COMPLETED: $className.$methodName")
      println(s"    Duration: ${duration}ms")
      println(s"    Result: ${formatValue(result)}")
      println("=" * 80)
      println()
      
      result
      
    } catch {
      case e: Exception =>
        // Calculate duration even on error
        val duration = System.currentTimeMillis() - startTime
        
        // PRINT: Method end (error)
        println("-" * 80)
        println(s"!!! FAILED: $className.$methodName")
        println(s"    Duration: ${duration}ms")
        println(s"    Error: ${e.getMessage}")
        println("=" * 80)
        println()
        
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
        // Handle Spark DataFrame
        case df: org.apache.spark.sql.DataFrame => 
          s"DataFrame[columns=${df.columns.length}, partitions=${df.rdd.getNumPartitions}]"
        
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
```


#### Short summary: 

empty definition using pc, found symbol in pc: 