package com.softwaremill.example

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive0
import com.softwaremill.session._
import com.softwaremill.session.CsrfDirectives._
import com.softwaremill.session.CsrfOptions._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.typesafe.scalalogging.StrictLogging
import ru.mit.spbau.scala.data.UsersCredsActor
import ru.mit.spbau.scala.shared.Consts
import ru.mit.spbau.scala.shared.data.UserCredentials
import ru.mit.spbau.scala.data.UserCredsEvent
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await
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

    // setting up user credentials persistent actor
    val usersCredentialsActor = system.actorOf(Props[UsersCredsActor], name = "users_credentials")

    val routes =
        path("") {
            redirect(Consts.indexPagePath, StatusCodes.Found)
        } ~
            path("login") {
                logger.info("Login redirect")
                redirect(Consts.loginPagePath, StatusCodes.Found)
            } ~
            path("register") {
                logger.info("Register redirect")
                redirect(Consts.registerPagePath, StatusCodes.Found)
            } ~
            randomTokenCsrfProtection(checkHeader) {
                pathPrefix("api") {
                    path("register") {
                        post {
                            entity(as[String]) { e =>
                                val userCreds = upickle.default.read[UserCredentials](e)
                                logger.info(s"Registration request: $userCreds")
                                implicit val timeout = Timeout(5 seconds)
                                val future = usersCredentialsActor ? UserCredsEvent.CheckLogin(userCreds)
                                val doesUserExists = Await.result(future, timeout.duration).asInstanceOf[Boolean]
                                logger.info(s"User $userCreds already exists? $doesUserExists")
                                if (!doesUserExists) {
                                    logger.info(s"Accept registration $userCreds")
                                    usersCredentialsActor ! UserCredsEvent.Register(userCreds)
                                    complete(StatusCodes.OK, s"Succesffuly registered user ${userCreds.login}")
                                } else {
                                    logger.info(s"Reject registration $userCreds")
                                    complete(StatusCodes.OK, "User is already registered")
                                }
                            }
                        }
                    } ~
                        path("login") {
                            post {
                                entity(as[String]) { e =>
                                    val userCreds = upickle.default.read[UserCredentials](e)
                                    logger.info(s"Logging in user: $userCreds")
                                    setRepeatItSession(RepeatItSession(userCreds.login)) {
                                        setNewCsrfToken(checkHeader) { ctx =>
                                            ctx.complete(StatusCodes.OK)
                                        }
                                    }
                                }
                            }
                        } ~
                        path("logout") {
                            post {
                                userSession { session =>
                                    myInvalidateSession { ctx =>
                                        logger.info(s"Logging out $session")
                                        ctx.complete(StatusCodes.OK)
                                    }
                                }
                            }
                        } ~
                        // This should be protected and accessible only when logged in
                        path("current_login") {
                            get {
                                logger.info("Current login get request.")
                                userSession { session =>
                                    ctx =>
                                        logger.info("Current session: " + session)
                                        ctx.complete(StatusCodes.OK, session.username)
                                }
                            }
                        } ~
                        path("get_cards") {
                            get {
                                userSession { session =>
                                    ctx =>
                                        ctx.complete(StatusCodes.OK, "asdsa")
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
        (un: String) => Try {
            RepeatItSession(un)
        }
    )
}