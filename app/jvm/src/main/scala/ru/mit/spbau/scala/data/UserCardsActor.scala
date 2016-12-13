package ru.mit.spbau.scala.data

import akka.persistence.{PersistentActor, SnapshotOffer}
import com.typesafe.scalalogging.StrictLogging
import ru.mit.spbau.scala.shared.data.CardToRepeatData

/**
  * Event, which may happen with user cards
  */
sealed trait CardsActorEvent
object CardsActorEvent {
    case class NewCard(card: CardToRepeatData) extends CardsActorEvent
    case class DeleteCard(cardId: Integer) extends CardsActorEvent
    case class CardChanged(cardId: Integer, newCardData: CardToRepeatData) extends CardsActorEvent
    case class GetCards() extends CardsActorEvent
    case class Shutdown() extends CardsActorEvent
}


/**
  * Actor, which persists user cards information
  * @param userId id of the user, which cards are persisted
  */
class UserCardsActor(val userId: String) extends PersistentActor with StrictLogging {
    override def persistenceId: String = s"cards_persistence_for_user_$userId"

    var state = UserCardsActor.State()

    override def receiveRecover: Receive = {
        case SnapshotOffer(_, snapshot: UserCardsActor.State) => state = snapshot
        case e: CardsActorEvent => updateState(e)
    }

    override def receiveCommand: Receive = {
        case "snap" => saveSnapshot(state)
        case CardsActorEvent.Shutdown => context.stop(self)

        case e: CardsActorEvent.NewCard =>
            logger.info("Got new card message")
            val id = state.size
            persist(e)(updateState)
        case e: CardsActorEvent.DeleteCard =>
            logger.info("Got delete card message")
            if (!state.cards.contains(e.cardId)) {
                throw new IllegalArgumentException(s"card with such id does not exists: ${e.cardId}" )
            }
            persist(e)(updateState)
        case e: CardsActorEvent.CardChanged =>
            logger.info("Got change card message")
            if (!state.cards.contains(e.cardId)) {
                throw new IllegalArgumentException(s"card with such id does not exists: ${e.cardId}" )
            }
            persist(e)(updateState)

        case CardsActorEvent.GetCards =>
            logger.info("Got get cards message")
            sender() ! state.cards
    }

    def updateState(e: CardsActorEvent): Unit = e match {
        case CardsActorEvent.CardChanged(cardId, newCardData) => state = state.cardChanged(cardId, newCardData)
        case CardsActorEvent.DeleteCard(id) => state = state.cardDeleted(id)
        case CardsActorEvent.NewCard(cardData) => state = state.cardAdded(state.size, cardData)
        case _ => logger.error("Unknown update state msg")
    }
}

object UserCardsActor {
    /**
      * State of cards persistent actor, which just holds a map of cards
      */
    case class State(cards: Map[Integer, CardToRepeatData] = Map.empty) {
        def cardAdded(id: Integer, cardData: CardToRepeatData): State = {
            copy(cards.updated(id, cardData))
        }

        def cardChanged(id: Integer, newCardData: CardToRepeatData): State = {
            copy(cards.updated(id, newCardData))
        }

        def cardDeleted(id: Integer): State = {
            copy(cards - id)
        }

        def size: Int = cards.size
    }
}
