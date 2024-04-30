package objektwerks

import java.time.LocalDateTime

sealed trait Entity

final case class Customer(id: Int = 0,
                          name: String,
                          address: String,
                          phone: String, 
                          email: String) extends Entity

final case class Role(name: String) extends Entity

final case class Contractor(id: Int = 0, 
                            customerId: Int, 
                            name: String, 
                            role: String) extends Entity

object Recurrence extends Enumeration {
  type Recurrence = Value
  val once, weekly, biweekly, monthly, quarterly, semiannual, annual = Value
}

import Recurrence._
final case class Task(id: Int = 0, 
                      contractorId: Int, 
                      task: String, 
                      recurrence: Recurrence, 
                      started: LocalDateTime = LocalDateTime.now, 
                      completed: LocalDateTime = LocalDateTime.now) extends Entity

final case class Supplier(id: Int = 0, 
                          name: String, 
                          address: String, 
                          phone: String, 
                          email: String) extends Entity
 
final case class ContractorSupplier(contractorId: Int, supplierId: Int) extends Entity