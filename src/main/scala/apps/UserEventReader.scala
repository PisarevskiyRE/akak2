package apps

import akka.{Done, NotUsed}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.alpakka.cassandra.scaladsl.CassandraSessionRegistry
import akka.stream.scaladsl.{Sink, Source}
import model._

import java.time.temporal.ChronoUnit
import scala.concurrent.Future


object UserEventReader {

  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "UserEventReaderSystem")

  //чтение журнала
  val readJournal = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  val persistenceIds: Source[String, NotUsed] = readJournal.persistenceIds()
  val consumptionSink = Sink.foreach(println)
  val connectedGraph = persistenceIds.to(consumptionSink)


  import system.executionContext

  val session = CassandraSessionRegistry(system).sessionFor("akka.projection.cassandra.session-config")


  def insertUserRow(user: User): Future[Unit] = {
    val User(id, login, firstName, lastName) = user

    val userFuture = session.executeWrite(
      "INSERT INTO app.users (id, login, firstName, lastName) VALUES " +
        s"('$id', '$login', '$firstName', '$lastName')"
    ).recover(e => println(s"Ошибка записи в базу: ${e}"))

    Future.sequence(userFuture :: Nil).map(_ => ())
  }

  def deleteUserRow(user: User): Future[Unit] = {
    val User(id, login, firstName, lastName)  = user

    val userFuture = session.executeWrite(
      "DELETE FROM app.users WHERE " +
        s"id='$id' "
    ).recover(e => println(s"reservation removal for date failed: ${e}"))

    Future.sequence(userFuture :: Nil).map(_ => ())
  }


  val eventsForUser = readJournal
    .eventsByPersistenceId("testUsers", 0, Long.MaxValue)
    .map(_.event)
    .map{
      case UserCreated(user) =>
        println(s"Пользователь создан $user")
        insertUserRow(user)
      case UserUpdated(oldUser,newUser) =>
        println(s"Пользователь обновлен с $oldUser на $newUser")
        for {
          _ <- deleteUserRow(oldUser)
          _ <- insertUserRow(newUser)
        } yield ()
      case UserDeleted(user) =>
        println(s"Пользователь удален $user")
        deleteUserRow(user)
    }


  def main(args: Array[String]): Unit = {
    eventsForUser.to(Sink.ignore).run()
  }
}
