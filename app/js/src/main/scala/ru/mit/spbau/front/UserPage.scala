package ru.mit.spbau.front

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.jquery.jQuery
import ru.mit.spbau.scala.shared.Consts
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event

/**
  * Main page, which user sees after login
  */
object UserPage {
    def setupUserPage(username: String): Unit = {
        jQuery("#logged-in-username").html(s"Logged as <b>$username</b>")

        // logout button setup
        val logoutBtn = dom.document.getElementById("logout-btn").asInstanceOf[html.Button]
        logoutBtn.onclick = (e: Event) => {
            LoginManagement.doLogout()
        }
    }
}
