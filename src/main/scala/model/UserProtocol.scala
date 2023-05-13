package model

import akka.actor.typed.ActorRef



// Команды пользоватлей
sealed trait UserCommand
case class CreateUser(login: String, firstName: String, lastName: String, replyTo: ActorRef[Any]) extends UserCommand
case class ChangeUser(id: Int, firstName: String, lastName: String, replyTo: ActorRef[Any]) extends UserCommand
case class DeleteUser(id:Int,  replyTo: ActorRef[Any]) extends UserCommand


//события пользователей
sealed trait UserEvent
case class UserCreated(user: User) extends UserEvent
case class UserUpdated(oldUser: User, newUser: User) extends UserEvent
case class UserDeleted(user: User) extends UserEvent



case class UserCommandFailure(reason: String)