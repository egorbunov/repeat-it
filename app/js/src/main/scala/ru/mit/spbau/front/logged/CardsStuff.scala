package ru.mit.spbau.front.logged
import ru.mit.spbau.scala.shared.data.CardToRepeatData

import scalatags.Text.TypedTag
import scalatags.Text.all._

object CardsStuff {
    def getCardTemplate(cardId: Int, cardData: CardToRepeatData): TypedTag[String] = {
        div(
            `class` := "row card-row",
            id := getCardRowId(cardId),
            div(
                `class`:="card",
                div(
                    `class` := "question-card",
                    div(
                        `class` := "card-image",
                    div(
                        `class` := "card-fake-img"
                    ),
                    span(
                        `class`:="card-title",
                        "Question"
                    )
                    ),
                    div(
                        `class` := "card-content",
                        p(
                            cardData.frontSide
                        )
                    )
                ),
                div(
                    hidden,
                    `class` := "answer-card",
                    div(
                        `class` := "card-image",
                        div(
                            `class` := "card-fake-img"
                        ),
                        span(
                            `class`:="card-title",
                            "Answer"
                        )
                    ),
                    div(
                        `class` := "card-content",
                        p(
                            cardData.backSide
                        )
                    )
                )
            )
        )
    }

    def getCardRowId(cardId: Int): String = s"card-row-$cardId"

    val answerMainBackgroundColor = "#99ff99"
    val questionMainBackgroundColor = "#ffc2b3"
}
