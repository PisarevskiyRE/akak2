package apps

import actor.UserSupervisor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._
import model._


object TestActors {

  def main(args: Array[String]): Unit = {

    // актор логов
    val simpleLogger = Behaviors.receive[Any] { (ctx, message) =>
      ctx.log.info(s"[==Лог==] $message")
      Behaviors.same
    }



    val root = Behaviors.setup[String] { ctx =>
      val logger = ctx.spawn(simpleLogger, "logger")
      val users = ctx.spawn(UserSupervisor("testUsers"), "testUsers")

      //users ! CreateUser("User1", "FirstName1", "LastName1", logger)
      //users ! UpdateUser("8012cbe5-1d1b-4b62-bcea-f3318624608d", "FirstName2", "LastName2", logger )
      //users ! CreateUser("User2", "FirstName3", "LastName3", logger)
      //users ! DeleteUser("b90d2168-95bc-41b2-b2a9-1bec6cd374c2", logger)


      Behaviors.same
    }


    val system = ActorSystem(root, "TestUsers")

    import system.executionContext
    system.scheduler.scheduleOnce(5.seconds, () => system.terminate())
  }
}
