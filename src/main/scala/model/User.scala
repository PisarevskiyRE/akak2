package model

import io.circe.Json

import java.util.UUID

case class User(id: String, login: String, firstName: String, lastName: String){

  def isExists(another: User): Boolean =
    this.login == another.login

  // для работы с сетом вида (set + user1 - user2)
  override def equals(obj: Any): Boolean = obj match {
    case User(`id`, _, _, _) =>  true
    case _ => false
  }

  override def hashCode(): Int = id.hashCode
}

object User {

  def make(login: String, firstName: String, lastName: String): User =
    User(UUID.randomUUID().toString, login, firstName, lastName)

}