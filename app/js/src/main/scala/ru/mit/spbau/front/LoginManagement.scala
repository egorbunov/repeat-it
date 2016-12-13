package ru.mit.spbau.front

import com.couchmate.jscookie.Cookies
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import org.scalajs.jquery.jQuery
import ru.mit.spbau.front.logged.UserPage
import ru.mit.spbau.scala.shared.data.UserCredentials
import ru.mit.spbau.scala.shared.{ApiStatus, Consts}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}


object LoginManagement {
    /**
      * Entry point
      *
      * Validates if user is logged in and in case of failure redirects
      * to login page
      */
    def validateLogin(): Unit = {
        doIfLoggedIn(
            onSuccess = (username: String) => {
                Routing.switchPageContents(
                    Consts.userPagePath,
                    () => {UserPage.setupUserPage(username)},
                    Consts.globalPlaceholderId
                )

            },
            onFailure = (_: Throwable) => {
                dom.window.console.log(s"Redirecting to login page...")
                Routing.switchPageContents(
                    Consts.loginPagePath,
                    setupLoginPage,
                    Consts.globalPlaceholderId)
            })
    }

    def doIfLoggedIn(onSuccess: (String) => Unit, onFailure: (Throwable) => Unit): Unit = {
        Ajax.get(
            url = "/api/current_login",
            headers = sessionTokenHeader
        ).onComplete({
            case Success(xhr) =>
                dom.window.console.log(s"Current login = ${xhr.responseText}")
                onSuccess(xhr.responseText)
            case Failure(t) =>
                onFailure(t)
        })
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

    def doLogout(): Unit = {
        Ajax.post(
            url = "/api/logout",
            headers = sessionTokenHeader
        ).onComplete({
            case Success(xhr) =>
                dom.window.alert("Bye-bye!")
                Routing.switchPageContents(
                    Consts.loginPagePath,
                    LoginManagement.setupLoginPage,
                    Consts.globalPlaceholderId
                )
            case Failure(t) =>
                dom.console.error(s"Failed to logout: ${t.getMessage}")
        })
    }

    /**
      * Setup button listeners for login page
      */
    def setupLoginPage(): Unit = {
        val loginBtn = dom.document.getElementById("sign-in-btn").asInstanceOf[html.Button]
        val loginForm = dom.document.getElementById("login-form").asInstanceOf[html.Form]
        loginForm.onsubmit = {
            (e: Event) => {
                e.preventDefault()
                // do not refresh the page, man!
                val user = jQuery("#login-user").value().toString
                val password = jQuery("#login-password").value().toString
                dom.window.console.log(s"$user $password")
                val credentials = UserCredentials(user, password)

                Ajax.post(
                    url = "/api/login",
                    data = upickle.default.write(credentials),
                    headers = sessionTokenHeader
                ).onSuccess({ case xhr =>
                    if (xhr.status == ApiStatus.OK) {
                        Routing.switchPageContents(
                            Consts.userPagePath,
                            ()=>{UserPage.setupUserPage(user)},
                            Consts.globalPlaceholderId
                        )
                    } else if (xhr.status == ApiStatus.userNotRegistered) {
                        dom.window.alert("Wrong username or password (no user with given creds)")
                    } else {
                        dom.console.error("Unknown response code on login")
                    }
                }
                )
            }
        }

        val createNewAccount = dom.document.getElementById("create-new-acc-btn").asInstanceOf[html.Link]
        createNewAccount.onclick = {
            (e: Event) => {
                e.preventDefault()
                Routing.switchPageContents(
                    Consts.registerPagePath,
                    RegistrationPage.setupRegisterPage,
                    Consts.globalPlaceholderId
                )
            }
        }
    }
}
