package ru.mit.spbau.front.logged

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import org.scalajs.jquery.{JQueryEventObject, jQuery}
import ru.mit.spbau.front.{LoginManagement, RegistrationPage, Routing}
import ru.mit.spbau.scala.shared.Consts
import ru.mit.spbau.scala.shared.data.{CardImportancePolicy, CardToRepeatData}

object AddCardPage {
    def setupAddCardPage(): Unit = {
        val addCardForm = dom.document.getElementById("add-card-form")
            .asInstanceOf[html.Button]
        addCardForm.onsubmit = (e: Event) => {
            e.preventDefault()
            val frontSide = jQuery("#front-side").value().toString
            val backSide = jQuery("#back-side").value().toString
            val policy = {
                if (jQuery("#HIGH").is(":checked")) {
                    CardImportancePolicy.HIGH
                } else if (jQuery("#MEDIUM").is(":checked")) {
                    CardImportancePolicy.MEDIUM
                } else {
                    CardImportancePolicy.LOW
                }
            }
            val newCardData = CardToRepeatData(
                frontSide,
                backSide,
                policy
            )
        }
    }
}
