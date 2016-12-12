package ru.mit.spbau.front

import com.couchmate.jscookie.Cookies

import scala.scalajs.js.JSApp
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.jquery.jQuery
import ru.mit.spbau.scala.shared.Consts
import ru.mit.spbau.scala.shared.data.UserCredentials

object RepeatItApp extends JSApp {
    override def main(): Unit = {
        LoginManagement.validateLogin()
    }
}
