package ru.mit.spbau.scala.service

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import ru.mit.spbau.scala.shared.ApiStatus

/**
  * Created by: Egor Gorbunov
  * Date: 12/13/16
  * Email: egor-mailbox@ya.com
  */
object ApiStatusCode {
    val OK = StatusCodes.OK
    val userAlreadyExists: StatusCode =
        StatusCodes.custom(ApiStatus.userAlreadyExists, "User already exists")
    val userNotRegistered: StatusCode =
        StatusCodes.custom(ApiStatus.userNotRegistered, "User with given credentials not registered")
}
