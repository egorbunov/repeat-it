package ru.mit.spbau.scala.data

import akka.persistence.{PersistentActor, SnapshotOffer}
import com.typesafe.scalalogging.StrictLogging
import ru.mit.spbau.scala.shared.data.{CardToRepeatData, UserCredentials}


/**
  * Actor, which persists registered users
  */
class UsersCredsActor extends PersistentActor with StrictLogging {
    override def persistenceId: String = "users_persistence_actor_42"

    var state = UsersCredsActor.State()

    override def receiveRecover: Receive = {
        case SnapshotOffer(_, snapshot: UsersCredsActor.State) => state = snapshot
    }

    override def receiveCommand: Receive = {
        case UserCredsEvent.CheckLogin(creds) =>
            logger.info(s"Got check login message: $creds")
            val usr = state.usersCreds.get(creds.login)
            val result = usr.exists(uc => {
                uc.password == creds.password
            })
            sender() ! result

        case e: UserCredsEvent.Register =>
            logger.info(s"Got register message: ${e.creds}")
            persist(e) { event =>
                state = state.userAdded(event.creds)
            }
    }
}

sealed trait UserCredsEvent

object UserCredsEvent {

    case class Register(creds: UserCredentials) extends UserCredsEvent

    case class CheckLogin(creds: UserCredentials) extends UserCredsEvent

}

object UsersCredsActor {

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
