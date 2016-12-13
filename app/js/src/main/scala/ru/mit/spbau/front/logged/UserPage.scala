package ru.mit.spbau.front.logged

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import org.scalajs.jquery.jQuery
import ru.mit.spbau.front.{LoginManagement, Routing}
import ru.mit.spbau.scala.shared.Consts
import ru.mit.spbau.scala.shared.data.CardToRepeatData

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


/**
  * Main page, which user sees after login
  */
object UserPage {
    var userCards: Map[Int, CardToRepeatData] = Map.empty

    def setupUserPage(username: String): Unit = {
        jQuery("#logged-in-username").html(s"Logged as <b>$username</b>")

        setupLogoutButton()
        setupDoRepeatButton()
        setupAddCardButton()
        setupUserCards()
    }

    private def setupLogoutButton(): Unit = {
        val logoutBtn = dom.document.getElementById("logout-btn").asInstanceOf[html.Button]
        logoutBtn.onclick = (e: Event) => {
            LoginManagement.doLogout()
        }
    }

    private def setupDoRepeatButton(): Unit = {
        val doRepeatBtn = dom.document.getElementById("do-repeat-btn")
            .asInstanceOf[html.Button]
        doRepeatBtn.onclick = (_: Event) => {
            Routing.switchPageContents(
                Consts.userDoRepeatPath,
                DoRepeatPage.setupDoRepeatPage,
                Consts.userPlaceholderId
            )
        }
    }

    private def setupAddCardButton(): Unit = {
        val addNewCardBtn = dom.document.getElementById("add-new-card-btn")
            .asInstanceOf[html.Button]
        addNewCardBtn.onclick = (_: Event) => {
            Routing.switchPageContents(
                Consts.userAddCardPath,
                AddCardPage.setupAddCardPage,
                Consts.userPlaceholderId
            )
        }
    }

    private def setupUserCards(): Unit = {
        Ajax.get(
            url = "/api/get_cards",
            headers = LoginManagement.sessionTokenHeader
        ).onSuccess({ case xhr =>
            userCards = upickle.default.read[Map[Int, CardToRepeatData]](xhr.responseText)
            dom.window.console.log(userCards.size)
            dom.window.console.log(userCards.toString())
        })
    }
}
