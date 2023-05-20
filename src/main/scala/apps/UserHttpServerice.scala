package apps

import actor.UserActor
import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.Http
import akka.util.Timeout
import http.UserRoutes
import model.UserCommand

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object UsersApp {

  def startHttpServer(users: ActorRef[UserCommand])(implicit system: ActorSystem[_]): Unit = {
    implicit val ec: ExecutionContext = system.executionContext
    val router = new UserRoutes(users)
    val routes = router.userRoutes

    val httpBindingFuture = Http().newServerAt("localhost",8080).bind(routes)
    httpBindingFuture.onComplete{
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Стартуем ${address.getHostString} - ${address.getPort}")

      case Failure(ex) =>
        system.log.info(s"ОШИБКА ${ex}")
        system.terminate()

    }
  }


  def main(args: Array[String]): Unit = {
    trait RootCommand
    case class RetrieveUsersActor(replyTo: ActorRef[ActorRef[UserCommand]]) extends RootCommand

    val rootBehavior: Behavior[RootCommand] = Behaviors.setup{ context =>
      val usersActor = context.spawn(UserActor("testUsers"),"testUsers")
      Behaviors.receiveMessage{
        case RetrieveUsersActor(replyTo) =>
          replyTo ! usersActor
          Behaviors.same
      }
    }

    implicit val system: ActorSystem[RootCommand] = ActorSystem(rootBehavior, "UsersSystem")
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val ec: ExecutionContext = system.executionContext

    val userActorFuture: Future[ActorRef[UserCommand]] = system.ask(replyTo => RetrieveUsersActor(replyTo))
    userActorFuture.foreach(startHttpServer)

  }
}


/*
http get localhost:8080/users/be240121-b782-40fe-8928-28559a92472c
http post localhost:8080/users login=testHTTPuser1 firstName=u1 lastName=u2

 */