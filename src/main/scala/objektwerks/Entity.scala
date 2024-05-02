package objektwerks

import java.time.LocalDateTime

sealed trait Entity

object Entity:
  given Ordering[Customer] = Ordering.by[Customer, String](c => c.name)
  given Ordering[Role] = Ordering.by[Role, String](r => r.name)
  given Ordering[Contractor] = Ordering.by[Contractor, String](c => c.name)
  given Ordering[Task] = Ordering.by[Task, String](t => t.task)

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

enum Recurrence:
  case once, weekly, biweekly, monthly, quarterly, semiannual, annual

final case class Task(id: Int = 0, 
                      contractorId: Int, 
                      task: String, 
                      recurrence: String, 
                      started: String = LocalDateTime.now.toString, 
                      completed: String = LocalDateTime.now.toString) extends Entity

final case class Supplier(id: Int = 0, 
                          name: String, 
                          address: String, 
                          phone: String, 
                          email: String) extends Entity
 
final case class ContractorSupplier(contractorId: Int, supplierId: Int) extends Entity