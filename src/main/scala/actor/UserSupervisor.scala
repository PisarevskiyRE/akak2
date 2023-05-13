package actor

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import model._


// главный актор
object UserSupervisor {

  case class UserState(users: Set[User])


  def userCommandHandler(userId: String): (UserState, UserCommand) => Effect[UserEvent, UserState] = (state, command) =>
    command match {
      case CreateUser(login, firstName, lastName, replyTo) =>
        // создаем пользователя
        val newUser = User(login,firstName,lastName)
        val conflictedUsers: Option[User] = state.users.find(f => f.ifExists(newUser))

        // если нет конфликтов пишем в касандру событие
        if (conflictedUsers.isEmpty){
          Effect.persist(UserCreated(newUser))
            .thenReply(replyTo)(_ => UserCreated(newUser)) // отвечаем что создали
        } else {
          // иначе сообщаем об ошибке
          Effect.reply(replyTo)(UserCommandFailure("Пользователь с таким логином уже существует."))
        }

    }

  def userEventHandler(userId: String): (UserState, UserEvent) => UserState = ???

  def apply(userId: String): Behavior[UserCommand] =
    EventSourcedBehavior[UserCommand, UserEvent, UserState](
      persistenceId = PersistenceId.ofUniqueId(userId),
      emptyState = UserState(Set()),
      commandHandler = userCommandHandler(userId),
      eventHandler = userEventHandler(userId)
    )
}
