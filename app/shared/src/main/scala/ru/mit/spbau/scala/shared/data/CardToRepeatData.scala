package ru.mit.spbau.scala.shared.data

object CardImportancePolicy {
    type Type = Int

    val LOW: Type = 0
    val MEDIUM: Type = 1
    val HIGH: Type = 2
}

/**
  * Card with data to repeat
  *
  * @param frontSide question or anything else, which user wants to learn the answer for
  * @param backSide the answer or anything else to be learned
  * @param lastRepeatTime last time the card was repeated
  * @param repeatPolicy repeating policy, which should control how often this card should be repeated
  */
case class CardToRepeatData(val frontSide: String,
                       val backSide: String,
                       val repeatPolicy: CardImportancePolicy.Type,
                       val lastRepeatTime: Long = Long.MinValue,
                       val lastSuccessfulRepeatTime: Long = Long.MinValue) {
}
