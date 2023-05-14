package model

import akka.actor.typed.ActorRef

import scala.concurrent.Future



trait UserProtocol


// команды пользоватлей
sealed trait UserCommand
case class CreateUser(login: String, firstName: String, lastName: String, replyTo: ActorRef[UserResponse]) extends UserCommand
case class UpdateUser(id: String, firstName: String, lastName: String, replyTo: ActorRef[UserResponse]) extends UserCommand
case class DeleteUser(id: String, replyTo: ActorRef[UserResponse]) extends UserCommand

case class GetUser(id: String, replyTo: ActorRef[UserResponse]) extends UserCommand


// события пользователей
sealed trait UserEvent
case class UserCreated(user: User) extends UserEvent
case class UserUpdated(oldUser: User, newUser: User) extends UserEvent
case class UserDeleted(user: User) extends UserEvent
case class UserGet(user: User) extends UserEvent

case class UserCommandFailure(reason: String) extends UserResponse
case class UserFailureResponse(reason: String) extends UserResponse

// ответы для rest по пользователю
sealed trait UserResponse
case class UserCreatedResponse(id: String) extends UserResponse
case class UserUpdatedResponse(maybeUserData: Option[User]) extends UserResponse
case class UserDeletedResponse(maybeUserData: Option[User]) extends UserResponse
case class UserGetResponse(maybeUserData: Option[User]) extends UserResponse


// для парсинга запроса по rest
case class UserCreateRequest(login: String, firstName: String, lastName: String) {
  def toCommand(replyTo: ActorRef[UserResponse]): UserCommand = CreateUser(login, firstName, lastName, replyTo)
}

case class UserUpdateRequest(firstName: String, lastName: String) {
  def toCommand(id:String, replyTo: ActorRef[UserResponse]): UserCommand = UpdateUser(id, firstName, lastName, replyTo)
}

case class UserDeleteRequest(login: String, firstName: String, lastName: String) {
  def toCommand(replyTo: ActorRef[UserResponse]): UserCommand = CreateUser(login, firstName, lastName, replyTo)
}



