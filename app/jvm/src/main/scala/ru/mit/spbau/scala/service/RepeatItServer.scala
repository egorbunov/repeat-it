package com.softwaremill.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directive0
import com.softwaremill.session._
import com.softwaremill.session.CsrfDirectives._
import com.softwaremill.session.CsrfOptions._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.typesafe.scalalogging.StrictLogging
import ru.mit.spbau.scala.shared.Consts

import scala.io.StdIn
import scala.util.Try

object RepeatItServer extends App with StrictLogging {
    implicit val system = ActorSystem("my_app")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val sessionConfig = SessionConfig.default(SessionUtil.randomServerSecret())
    implicit val sessionManager = new SessionManager[RepeatItSession](sessionConfig)
    implicit val refreshTokenStorage = new InMemoryRefreshTokenStorage[RepeatItSession] {
        def log(msg: String): Unit = logger.info(msg)
    }

    def setRepeatItSession(v: RepeatItSession): Directive0 = setSession(refreshable, usingCookies, v)
    val userSession = requiredSession(refreshable, usingCookies)
    val myInvalidateSession = invalidateSession(refreshable, usingCookies)

    val routes =
        path("") {
            redirect(Consts.indexPagePath, Found)
        } ~
        path("login") {
            logger.info("Login redirect")
            redirect(Consts.loginPagePath, Found)
        } ~
        path("register") {
            logger.info("Register redirect")
            redirect(Consts.registerPagePath, Found)
        } ~
            randomTokenCsrfProtection(checkHeader) {
                pathPrefix("api") {
                    path("login") {
                        post {
                            entity(as[String]) { body =>
                                logger.info(s"Logging in $body")

                                setRepeatItSession(RepeatItSession(body)) {
                                    setNewCsrfToken(checkHeader) { ctx => ctx.complete("ok") }
                                }
                            }
                        }
                    } ~
                        path("logout") {
                            post {
                                userSession { session =>
                                    myInvalidateSession { ctx =>
                                        logger.info(s"Logging out $session")
                                        ctx.complete("ok")
                                    }
                                }
                            }
                        } ~
                        // This should be protected and accessible only when logged in
                        path("current_login") {
                            get {
                                userSession { session => ctx =>
                                    logger.info("Current session: " + session)
                                    ctx.complete(session.username)
                                }
                            }
                        } ~
                        path("get_cards") {
                            get {
                                userSession { session => ctx =>
                                    ctx.complete("asdsa")
                                }
                            }
                        }
                } ~
                    pathPrefix("site") {
                        getFromResourceDirectory("")
                    }
            }

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println("Server started, press enter to stop. Visit http://localhost:8080.")
    StdIn.readLine()

    import system.dispatcher
    bindingFuture
        .flatMap(_.unbind())
        .onComplete { _ =>
            system.terminate()
            println("Server stopped")
        }
}

case class RepeatItSession(username: String)

object RepeatItSession {
    implicit def serializer: SessionSerializer[RepeatItSession, String] = new SingleValueSessionSerializer(
        _.username,
        (un: String) => Try { RepeatItSession(un) }
    )
}