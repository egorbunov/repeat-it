package ru.mit.spbau.front

import com.couchmate.jscookie.Cookies

import scala.scalajs.js.JSApp
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.jquery.jQuery
import ru.mit.spbau.scala.shared.Consts

object RepeatItApp extends JSApp {
    override def main(): Unit = {
        val curPath = dom.window.location.pathname
        dom.window.console.log(curPath)
        curPath match {
            case Consts.loginPagePath => setupLoginPage()
            case Consts.indexPagePath => setupMainPage()
            case Consts.registerPagePath => setupRegisterPage()
        }
    }

    def setupLoginPage(): Unit = {
//        jQuery("#sign-in-btn")
        dom.window.console.log("Setting up login page")
    }

    def setupRegisterPage(): Unit = {
        dom.window.console.log("Setting up register page")
    }

    def setupMainPage(): Unit = {
        validateLogin()
        dom.window.console.log("Setting up main page")
    }

    /**
      * Validates if user is logged in
      */
    def validateLogin(): Unit = {
        Ajax.get(
            url = "/api/current_login",
            headers = sessionTokenHeader
        ).onComplete(p => {
            if (p.isSuccess) {
                dom.window.console.log("Success")
            } else {
                dom.window.console.log("Not logged in, redirecting...")
                dom.window.location.href = Consts.loginPagePath
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
}
