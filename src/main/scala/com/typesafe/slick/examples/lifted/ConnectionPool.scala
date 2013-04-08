package com.typesafe.slick.examples.lifted

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import com.mchange.v2.c3p0.ComboPooledDataSource

/* Connection pooling with c3p0 example.
 * For a more detailed explanation see:
 * http://fernandezpablo85.github.io/2013/04/07/slick_connection_pooling.html
 */
object ConnectionPool extends App {

  case class User(id: Option[Int], name: String, last: String) {
    override def toString = last + ", " + name
  }

  object Users extends Table[User]("users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def lastName = column[String]("last_name")

    def * = (id.? ~ name ~ lastName) <> (User, User.unapply _)
  }

  // the pool can be fully configured.
  // http://www.mchange.com/projects/c3p0/
  val datasource = new ComboPooledDataSource
  datasource.setDriverClass("org.h2.Driver")
  datasource.setJdbcUrl("jdbc:h2:mem:test1")

  Database.forDataSource(datasource) withSession {

    // create schema.
    Users.ddl.create

    // insert a few users.
    Users.insert(User(None, "pablo", "fernandez"))
    Users.insert(User(None, "noob", "saibot"))
    Users.insert(User(None, "vincent", "vega"))

    // query by last name.
    val lastName = "fernandez"
    val users = (for (u <- Users if u.lastName is lastName) yield (u)).list

    // print results.
    if (users.isEmpty)
      println(s"no users with last name '$lastName'")
    else {
      println(s"users with last name '$lastName':")
      users foreach { user =>
        println(user)
      }
    }
  }
}
