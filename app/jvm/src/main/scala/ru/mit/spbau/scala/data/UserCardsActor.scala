package ru.mit.spbau.scala.data

import akka.persistence.{PersistentActor, SnapshotOffer}
import ru.mit.spbau.scala.shared.data.CardToRepeatData

/**
  * Event, which may happen with user cards
  */
sealed trait CardsEvent
object CardsEvent {
    case class NewCard(card: CardToRepeatData)
    case class DeleteCard(cardId: Integer)
    case class CardChanged(cardId: Integer, newCardData: CardToRepeatData)
    case class GetCards()
}


/**
  * Actor, which persists user cards information
  * @param userId id of the user, which cards are persisted
  */
class UserCardsActor(val userId: String) extends PersistentActor {
    override def persistenceId: String = s"cards_persistence_for_user_$userId"

    var state = UserCardsActor.State()

    override def receiveRecover: Receive = {
        case SnapshotOffer(_, snapshot: UserCardsActor.State) => state = snapshot
    }

    override def receiveCommand: Receive = {
        case e: CardsEvent.NewCard =>
            val id = state.size
            persist(e) { event =>
                state = state.cardAdded(id, event.card)
            }
        case e: CardsEvent.DeleteCard =>
            if (!state.cards.contains(e.cardId)) {
                throw new IllegalArgumentException(s"card with such id does not exists: ${e.cardId}" )
            }
            persist(e) { event =>
                state = state.cardDeleted(event.cardId)
            }
        case e: CardsEvent.CardChanged =>
            if (!state.cards.contains(e.cardId)) {
                throw new IllegalArgumentException(s"card with such id does not exists: ${e.cardId}" )
            }
            persist(e) { event =>
                state = state.cardChanged(event.cardId, event.newCardData)
            }
        case CardsEvent.GetCards =>
            sender() ! state.cards
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
