package ru.mit.spbau.scala.shared.data

sealed trait CardImportancePolicy

object CardImportancePolicy {
    final case class Low() extends CardImportancePolicy
    final case class Medium() extends CardImportancePolicy
    final case class High() extends CardImportancePolicy

    val LOW = Low()
    val MEDIUM = Medium()
    val HIGH = High()
}

/**
  * Card with data to repeat
  *
  * @param frontSide question or anything else, which user wants to learn the answer for
  * @param backSide the answer or anything else to be learned
  * @param lastRepeatTime last time the card was repeated
  * @param repeatPolicy repeating policy, which should control how often this card should be repeated
  */
class CardToRepeatData(val frontSide: String,
                       val backSide: String,
                       val repeatPolicy: CardImportancePolicy,
                       val lastRepeatTime: Long = Long.MinValue,
                       val lastSuccessfulRepeatTime: Long = Long.MinValue) {
}
