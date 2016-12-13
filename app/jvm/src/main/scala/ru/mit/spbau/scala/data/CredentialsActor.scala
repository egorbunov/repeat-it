package ru.mit.spbau.scala.data

import akka.persistence.{PersistentActor, SnapshotOffer}
import com.typesafe.scalalogging.StrictLogging
import ru.mit.spbau.scala.shared.data.{CardToRepeatData, UserCredentials}


/**
  * Actor, which persists registered users
  */
class CredentialsActor extends PersistentActor with StrictLogging {
    override def persistenceId: String = "users_persistence_actor_42"

    var state = CredentialsActor.State()

    override def receiveRecover: Receive = {
        case SnapshotOffer(_, snapshot: CredentialsActor.State) => state = snapshot
        case e: UserCredsEvent => updateState(e)
    }

    override def receiveCommand: Receive = {
        case UserCredsEvent.CheckUsername(username) =>
            logger.info(s"Got check username message: $username")
            val usr = state.usersCreds.get(username)
            sender() ! usr.isDefined

        case UserCredsEvent.CheckCredentials(creds) =>
            logger.info(s"Got check credentials message: $creds")
            val usr = state.usersCreds.get(creds.login)
            val result = usr.exists(uc => {
                uc.password == creds.password
            })
            sender() ! result

        case e: UserCredsEvent.Register =>
            logger.info(s"Got register message: ${e.creds}")
            persist(e)(updateState)

        case UserCredsEvent.Shutdown => context.stop(self)
    }

    def updateState(e: UserCredsEvent): Unit = e match {
        case UserCredsEvent.Register(newCreds) =>
            state = state.userAdded(newCreds)
        case _ => logger.error("Unknown update state msg")
    }
}

sealed trait UserCredsEvent

object UserCredsEvent {

    case class Register(creds: UserCredentials) extends UserCredsEvent

    case class CheckCredentials(creds: UserCredentials) extends UserCredsEvent

    case class CheckUsername(username: String) extends UserCredsEvent

    case class Shutdown() extends UserCredsEvent

}

object CredentialsActor {

    case class State(usersCreds: Map[String, UserCredentials] = Map.empty) {
        def userAdded(userData: UserCredentials): State = {
            if (!usersCreds.contains(userData.login)) {
                copy(usersCreds.updated(userData.login, userData))
            } else {
                this
            }
        }

        def size: Int = usersCreds.size
    }

}
