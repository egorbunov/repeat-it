package ru.mit.spbau.front

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.jquery.jQuery
import ru.mit.spbau.scala.shared.Consts
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import ru.mit.spbau.scala.shared.data.UserCredentials

object RegistrationPage {
    def setupRegisterPage(): Unit = {
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
                ).onComplete( p => {
                    dom.window.alert(p.get.responseText)
                    if (p.isSuccess) {
                        dom.window.alert("OK!")
                    } else {
                        dom.window.alert("FAILED TO REGISTER =(")
                    }
                }
//

                )
            }
        }
    }
}
