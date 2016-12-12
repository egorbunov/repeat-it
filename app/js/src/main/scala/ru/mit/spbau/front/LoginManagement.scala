package ru.mit.spbau.front

import com.couchmate.jscookie.Cookies
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import org.scalajs.jquery.jQuery
import ru.mit.spbau.scala.shared.Consts
import ru.mit.spbau.scala.shared.data.UserCredentials
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


object LoginManagement {
    /**
      * Entry point
      *
      * Validates if user is logged in and in case of failure redirects
      * to login page
      */
    def validateLogin(): Unit = {
        Ajax.get(
            url = "/api/current_login",
            headers = sessionTokenHeader
        ).onComplete(p => {
            if (p.isSuccess) {
                dom.window.alert(s"Logged as ${p.get.response.toString}")
                dom.window.console.log("Success")
                Commons.switchToPage(Consts.userPagePath, UserPage.setupUserPage)
            } else {
                dom.window.alert("Not logged in, go login...")
                Commons.switchToPage(Consts.loginPagePath, setupLoginPage)
            }
        }
        )
    }

    /**
      * Gets session token from cookies
      */
    def sessionTokenHeader: Map[String, String] = {
        val token = Cookies.get("XSRF-TOKEN")
        if (!token.get.isEmpty) {
            dom.window.console.log(s"Setting csrf token: $token")
            return Map("X-XSRF-TOKEN" -> token.get)
        } else {
            dom.window.console.log("No csrf token")
        }
        Map.empty
    }

    /**
      * Setup button listeners for login page
      */
    def setupLoginPage(): Unit = {
        val loginBtn = dom.document.getElementById("sign-in-btn").asInstanceOf[html.Button]
        val loginForm = dom.document.getElementById("login-form").asInstanceOf[html.Form]
        loginForm.onsubmit = {
            (e: Event) => {
                e.preventDefault() // do not refresh the page, man!
                val user = jQuery("#login-user").value().toString
                val password = jQuery("#login-password").value().toString
                dom.window.console.log(s"$user $password")
                val credentials = UserCredentials(user, password)

                Ajax.post(
                    url = "/api/login",
                    data = upickle.default.write(credentials),
                    headers = sessionTokenHeader
                ).onComplete( p =>
                    Commons.switchToPage(Consts.userPagePath, UserPage.setupUserPage)
                )
            }
        }

        val createNewAccount = dom.document.getElementById("create-new-acc-btn").asInstanceOf[html.Link]
        createNewAccount.onclick = {
            (e: Event) => {
                e.preventDefault()
                Commons.switchToPage(Consts.registerPagePath, RegistrationPage.setupRegisterPage)
            }
        }
    }

}
