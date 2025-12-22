package com.company.analytics.models

import java.sql.Timestamp

/**
 * WHAT THIS DOES:
 * - Defines the structure of a sales record
 * - 'case class' automatically generates:
 *   * Constructor
 *   * Getters (no setters - immutable by default)
 *   * equals() and hashCode()
 *   * toString()
 *   * copy() method
 * 
 * JAVA EQUIVALENT (would need ~50 lines):
 * public class SalesRecord {
 *   private final String orderId;
 *   private final String customerId;
 *   // ... constructors, getters, equals, hashCode, toString
 * }
 * 
 * OR using Lombok:
 * @Data
 * @AllArgsConstructor
 * public class SalesRecord { ... }
 */
case class SalesRecord(
  orderId: String,           // These are fields AND constructor parameters
  customerId: String,
  productId: String,
  productName: String,
  category: String,
  quantity: Int,
  unitPrice: Double,
  totalAmount: Double,
  transactionDate: Timestamp,
  region: String,
  salesPerson: String
)

/**
 * Companion object (like static methods in Java)
 * Always has same name as the class
 * 
 * JAVA EQUIVALENT:
 * public class SalesRecord {
 *   public static SalesRecord fromCsv(String line) { ... }
 * }
 */
object SalesRecord {
  
  /**
   * Create SalesRecord from CSV line
   * SCALA NOTE: Notice how clean the code is - no explicit 'new' keyword
   */
  def fromCsv(line: String): SalesRecord = {
    // Split CSV line
    val parts = line.split(",")
    
    // Create instance - case class auto-generates apply() method
    // This is like calling: new SalesRecord(...)
    SalesRecord(
      orderId = parts(0),
      customerId = parts(1),
      productId = parts(2),
      productName = parts(3),
      category = parts(4),
      quantity = parts(5).toInt,           // String to Int conversion
      unitPrice = parts(6).toDouble,       // String to Double conversion
      totalAmount = parts(7).toDouble,
      transactionDate = Timestamp.valueOf(parts(8)),
      region = parts(9),
      salesPerson = parts(10)
    )
  }
  
  /**
   * Create SalesRecord with default test values
   */
  def sample(): SalesRecord = {
    SalesRecord(
      orderId = "ORD-001",
      customerId = "CUST-123",
      productId = "PROD-456",
      productName = "Laptop",
      category = "Electronics",
      quantity = 2,
      unitPrice = 1200.00,
      totalAmount = 2400.00,
      transactionDate = new Timestamp(System.currentTimeMillis()),
      region = "North",
      salesPerson = "John Doe"
    )
  }
}