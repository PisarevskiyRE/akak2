package actor

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import model._


object UserActor {

  case class UserState(users: Set[User])


  def userCommandHandler(userId: String): (UserState, UserCommand) => Effect[UserEvent, UserState] = (state, command) =>
    command match {
      // Создать пользователя
      case CreateUser(login, firstName, lastName, replyTo) =>
        // создаем пользователя
        val newUser = User.make(login,firstName,lastName)
        // проверяем существует ли
        val conflictedUsers: Option[User] = state.users.find(f => f.isExists(newUser))

        // если нет конфликтов пишем в касандру событие
        if (conflictedUsers.isEmpty){
          Effect.persist(UserCreated(newUser))
            .thenReply(replyTo)(_ => UserCreatedResponse(newUser.id)) // отвечаем что создали
        } else {
          // иначе сообщаем об ошибке
          Effect.reply(replyTo)(UserCommandFailure("Пользователь с таким логином уже существует."))
        }


      // Изменить пользователя
      case UpdateUser(id, firstName, lastName, replyTo) =>

        val oldUserOption: Option[User] = state.users.find( _.id == id)
        val newUserOption: Option[User] = oldUserOption
                  .map(u => u.copy(firstName = firstName, lastName = lastName))

        (oldUserOption, newUserOption) match {
          case (Some(oldU), Some(newU)) =>
            Effect
              .persist(UserUpdated(oldU, newU))
              .thenReply(replyTo)(_ => UserUpdatedResponse(Some(newU)))
          case _ =>
            Effect.reply(replyTo)(UserCommandFailure(s"Пользователя ${id} не возможно обновить: не найден."))
        }


      // Удалить пользователя
      case DeleteUser(id, replyTo) =>
        val userOption = state.users.find(_.id == id)
        userOption match {
          case Some(user) =>
            Effect.persist(UserDeleted(user)).thenReply(replyTo)(_ => UserDeletedResponse(Some(user)))
          case None =>
            Effect.reply(replyTo)(UserCommandFailure(s"Пользователя ${id} не возможно удалить: не найден."))
        }

      // получить пользователя
      case GetUser(id, replyTo) =>
        val userOption = state.users.find(_.id == id)
        userOption match {
          case Some(user) =>
            Effect.reply(replyTo)(UserGetResponse(Some(user)))
          case None =>
            Effect.reply(replyTo)(UserCommandFailure(s"Пользователя ${id} не возможно получить: не найден."))
        }
    }

  def userEventHandler(userId: String): (UserState, UserEvent) => UserState = (state, event) =>
    event match {
      case UserCreated(user) =>
        // добавляем пользователя в нутренне хранилище актора
        val newUserState = state.copy(users = state.users + user)
        println(s"Внутренне хранилище актора пользователей изменено на ->  $newUserState")
        newUserState
      case UserUpdated(oldUser, newUser) =>
        val newUserState = state.copy(users = state.users - oldUser + newUser)
        println(s"Внутренне хранилище актора пользователей изменено на ->  $newUserState")
        newUserState
      case UserDeleted(user) =>
        val newUserState = state.copy(users = state.users - user)
        println(s"Внутренне хранилище актора пользователей изменено на ->  $newUserState")
        newUserState
    }


  def apply(userId: String): Behavior[UserCommand] =
    EventSourcedBehavior[UserCommand, UserEvent, UserState](
      persistenceId = PersistenceId.ofUniqueId(userId),
      emptyState = UserState(Set()),
      commandHandler = userCommandHandler(userId),
      eventHandler = userEventHandler(userId)
    )
}
