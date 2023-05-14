package model

import akka.actor.typed.ActorRef



trait UserProtocol


// команды пользоватлей
sealed trait UserCommand
case class CreateUser(login: String, firstName: String, lastName: String, replyTo: ActorRef[Any]) extends UserCommand
case class UpdateUser(id: String, firstName: String, lastName: String, replyTo: ActorRef[Any]) extends UserCommand
case class DeleteUser(id: String, replyTo: ActorRef[Any]) extends UserCommand

case class GetUser(id: String, replyTo: ActorRef[UserProtocol]) extends UserCommand


// события пользователей
sealed trait UserEvent
case class UserCreated(user: User) extends UserEvent
case class UserUpdated(oldUser: User, newUser: User) extends UserEvent
case class UserDeleted(user: User) extends UserEvent


case class UserCommandFailure(reason: String) extends UserResponse


// ответы для rest по пользователю
sealed trait UserResponse

case class UserCreatedResponse(id: String) extends UserResponse
case class UserUpdatedResponse(maybeUserData: Option[User]) extends UserResponse
case class UserDeletedResponse(maybeUserData: Option[User]) extends UserResponse
case class GetUserResponse(maybeUserData: Option[Any]) extends UserResponse


