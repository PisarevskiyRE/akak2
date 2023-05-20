package apps

import actor.UserActor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._
import model._


object UserActorSimpleTest {

  def main(args: Array[String]): Unit = {

    // актор логов
    val simpleLogger = Behaviors.receive[Any] { (ctx, message) =>
      ctx.log.info(s"[==Лог==] $message")
      Behaviors.same
    }



    val root = Behaviors.setup[String] { ctx =>
      val logger = ctx.spawn(simpleLogger, "logger")
      val users = ctx.spawn(UserActor("testUsers"), "testUsers")

     // users ! CreateUser("User1", "FirstName1", "LastName1", logger)
     // users ! UpdateUser("be240121-b782-40fe-8928-28559a92472c", "FirstName2", "LastName2", logger )
    //  users ! CreateUser("User2", "FirstName3", "LastName3", logger)
    //  users ! DeleteUser("cfd746a0-117a-41fc-831c-c99f0d420efa", logger)
    //  users ! UpdateUser("be240121-b782-40fe-8928-28559a92472c", "FirstName3", "LastName3", logger )

      users ! GetUser("be240121-b782-40fe-8928-28559a92472c", logger)

      Behaviors.same
    }


    val system = ActorSystem(root, "TestUsers")

    import system.executionContext
    system.scheduler.scheduleOnce(5.seconds, () => system.terminate())
  }
}
