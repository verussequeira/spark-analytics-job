package com.company.analytics.models

/**
 * Customer profile data model
 * Demonstrates nested case classes and Option type
 */
case class CustomerProfile(
  customerId: String,
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String],        // Option = Java's Optional<String>
  address: Option[Address],     // Nested case class
  loyaltyPoints: Int,
  segment: String,              // "Premium", "Standard", "Basic"
  registrationDate: String
)

/**
 * Nested case class for address
 * SCALA NOTE: You can define multiple classes in one file
 */
case class Address(
  street: String,
  city: String,
  state: String,
  zipCode: String,
  country: String
)

object CustomerProfile {
  
  /**
   * SCALA CONCEPT: Pattern matching (like switch on steroids)
   * 
   * JAVA EQUIVALENT:
   * public static String getSegment(int points) {
   *   if (points >= 1000) return "Premium";
   *   else if (points >= 500) return "Standard";
   *   else return "Basic";
   * }
   */
  def calculateSegment(loyaltyPoints: Int): String = loyaltyPoints match {
    case p if p >= 1000 => "Premium"    // 'if' is a guard condition
    case p if p >= 500  => "Standard"
    case _              => "Basic"      // '_' is wildcard (default case)
  }
  
  /**
   * Create sample customer
   * Shows how to work with Option
   */
  def sample(): CustomerProfile = {
    CustomerProfile(
      customerId = "CUST-123",
      firstName = "Jane",
      lastName = "Smith",
      email = "jane.smith@email.com",
      phone = Some("555-1234"),           // Some() wraps a value in Option
      address = Some(Address(             // Nested case class
        street = "123 Main St",
        city = "San Francisco",
        state = "CA",
        zipCode = "94105",
        country = "USA"
      )),
      loyaltyPoints = 750,
      segment = calculateSegment(750),
      registrationDate = "2023-01-15"
    )
  }
  
  /**
   * Create customer without optional fields
   * Shows None (like Java's Optional.empty())
   */
  def sampleMinimal(): CustomerProfile = {
    CustomerProfile(
      customerId = "CUST-456",
      firstName = "John",
      lastName = "Doe",
      email = "john.doe@email.com",
      phone = None,              // None = no value (instead of null)
      address = None,
      loyaltyPoints = 100,
      segment = "Basic",
      registrationDate = "2024-01-01"
    )
  }
}