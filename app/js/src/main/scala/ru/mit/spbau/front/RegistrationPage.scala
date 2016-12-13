package ru.mit.spbau.front

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.jquery.jQuery
import ru.mit.spbau.scala.shared.{ApiStatus, Consts}
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.{XMLHttpRequest, html}
import org.scalajs.dom.raw.Event
import ru.mit.spbau.scala.shared.data.UserCredentials

import scala.util.{Failure, Success}

object RegistrationPage {
    def setupRegisterPage(): Unit = {
        setupRegisterSubmit()
        setupGoLoginPageButton()
    }

    private def setupRegisterSubmit(): Unit = {
        val registerForm = dom.document.getElementById("register-form").asInstanceOf[html.Form]
        registerForm.onsubmit = {
            (e: Event) => {
                e.preventDefault()

                val user = jQuery("#reg-username").value().toString
                val password = jQuery("#reg-password").value().toString
                dom.window.console.log(s"$user $password")
                val credentials = UserCredentials(user, password)

                Ajax.post(
                    url = "/api/register",
                    data = upickle.default.write(credentials),
                    headers = LoginManagement.sessionTokenHeader
                ).onSuccess({ case xhr =>
                    if (xhr.status == ApiStatus.OK) {
                        dom.window.alert("Success!")
                    } else if (xhr.status == ApiStatus.userAlreadyExists) {
                        dom.window.alert("User already registered =(")
                    } else {
                        dom.console.error("Unknown response status on register")
                    }
                })
            }
        }
    }

    private def setupGoLoginPageButton(): Unit = {
        val goLoginBtn = dom.document.getElementById("back-to-login-btn").asInstanceOf[html.Button]
        goLoginBtn.onclick = (_: Event) => {
            Routing.switchPageContents(
                Consts.loginPagePath,
                LoginManagement.setupLoginPage,
                Consts.globalPlaceholderId
            )
        }
    }
}
