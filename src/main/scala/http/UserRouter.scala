package http

import akka.actor.typed.ActorSystem
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.util._
import scala.concurrent.Future
import scala.concurrent.duration._
import cats.implicits._
import cats.data.Validated.{Invalid, Valid}
import model._

class UserRoutes(users: ActorRef[UserCommand])(implicit system: ActorSystem[_]) {

  implicit val timeout: Timeout = Timeout(5.seconds)

  def createUser(request: UserCreateRequest): Future[UserResponse] =
    users.ask(replyTo => request.toCommand(replyTo))

  def getUser(id: String): Future[UserResponse] =
    users.ask(replyTo => GetUser(id, replyTo))

  def updateUser(id:String, request: UserUpdateRequest): Future[UserResponse] =
    users.ask(replyTo => request.toCommand(id, replyTo))

  /*
      POST /users/
        Payload: запрос на создание пользователя
        Response:
          201 Создан
          location: /users/uuid

      GET /users/uuid
        Response:
          200 OK
            JSON детали юзера
          404 не найден

      PUT /users/uuid
        Payload: (field, value) as JSON
        Response:
          - 200 OK
            Payload: свойства пользователя JSON
          - 404 не найден
          - *400 не верный запрос
     */



  val userRoutes =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        post {
          // если парсится в UserCreationRequest
          // переводим реквест в команду актора
          // и отправляем, проверяем ответ и отправляет отвект http
          entity(as[UserCreateRequest]) { request =>
            onSuccess(createUser(request)) {
              case UserCreatedResponse(id) =>
                respondWithHeader(Location(s"/users/$id")) {
                  complete(StatusCodes.Created)
                }
            }
          }
        }
      } ~
        path(Segment) { id =>
          get {
            onSuccess(getUser(id)) {
              case UserGetResponse(Some(user)) =>
                complete(user)
              case UserGetResponse(None) =>
                complete(StatusCodes.NotFound, UserFailureResponse(s"Пользовать c ID{$id} не найден"))
            }
          } ~
            put {
              entity(as[UserUpdateRequest]) { request =>
                onSuccess(updateUser(id, request)) {
                  case UserUpdatedResponse(Some(user)) =>
                    complete(user)
                  case UserUpdatedResponse(None) =>
                    complete(StatusCodes.NotFound, UserFailureResponse(s"Пользовать c ID{$id} не найден"))
                }
              }
            }
        }
    }
}
