package ru.mit.spbau.front.logged

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import org.scalajs.jquery.jQuery
import ru.mit.spbau.front.LoginManagement
import ru.mit.spbau.scala.shared.data.{CardImportancePolicy, CardToRepeatData}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

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
            Ajax.post(
                url = "/api/add_new_card",
                data = upickle.default.write(newCardData),
                headers = LoginManagement.sessionTokenHeader
            ).onSuccess({ case xhr =>
                    dom.window.alert(xhr.responseText)
            })
        }
    }
}
