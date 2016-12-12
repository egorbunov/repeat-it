package ru.mit.spbau.scala.data

import akka.persistence.PersistentActor
import ru.mit.spbau.scala.shared.data.UserCredentials

/**
  * Actor, which persists registered users
  */
class UsersCredsActor extends PersistentActor {
    override def persistenceId: String = "users_persistence_actor_42"

    override def receiveRecover: Receive = ???

    override def receiveCommand: Receive = ???
}

sealed trait UserCredsEvent
object UserCredsEvent {
    case class Register(creds: UserCredentials) extends UserCredsEvent
    case class CheckLogin(creds: UserCredentials) extends UserCredsEvent
}

object UsersCredsActor {
    case class State(usersCreds: Map[String, UserCredentials])
}
