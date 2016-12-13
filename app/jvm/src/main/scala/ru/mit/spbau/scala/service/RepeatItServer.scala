package com.softwaremill.example

import akka.actor.{ActorRef, ActorSystem, Props}
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
import ru.mit.spbau.scala.data._
import ru.mit.spbau.scala.shared.{ApiStatus, Consts}
import ru.mit.spbau.scala.shared.data.{CardToRepeatData, UserCredentials}
import akka.pattern.ask
import akka.util.Timeout
import ru.mit.spbau.scala.service.ApiStatusCode

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
    val usersCredentialsActor = system.actorOf(Props[CredentialsActor], name = "users_persistence_actor_42")

    def getUserCardsActorName(username: String): String = s"user_${username}_cards_actor"

    var userCardsActorsMap = Map.empty[String, ActorRef]

    implicit val timeout = Timeout(5 seconds)

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
                                val future = usersCredentialsActor ? UserCredsEvent.CheckUsername(userCreds.login)
                                val doesUserExists = Await.result(future, timeout.duration).asInstanceOf[Boolean]
                                logger.info(s"User $userCreds already exists? $doesUserExists")
                                if (!doesUserExists) {
                                    logger.info(s"Accept registration $userCreds")
                                    usersCredentialsActor ! UserCredsEvent.Register(userCreds)
                                    complete(ApiStatusCode.OK)
                                } else {
                                    logger.info(s"Reject registration $userCreds")
                                    complete(ApiStatusCode.userAlreadyExists)
                                }
                            }
                        }
                    } ~
                        path("login") {
                            post {
                                entity(as[String]) { e =>
                                    val userCreds = upickle.default.read[UserCredentials](e)
                                    logger.info(s"Logging in user: $userCreds")

                                    val f = usersCredentialsActor ? UserCredsEvent.CheckCredentials(userCreds)
                                    val isOkCreds = Await.result(f, timeout.duration).asInstanceOf[Boolean]

                                    logger.info(s"Credentials $userCreds check status: $isOkCreds")

                                    if (!isOkCreds) {
                                        // ok...
                                        complete(ApiStatusCode.userNotRegistered)
                                    } else {
                                        // creating session and ...
                                        setRepeatItSession(RepeatItSession(userCreds.login)) {
                                            setNewCsrfToken(checkHeader) { ctx =>
                                                // creating user cards actor
                                                val userCardsActor = system.actorOf(
                                                    Props(new UserCardsActor(userCreds.login)),
                                                    name = getUserCardsActorName(userCreds.login)
                                                )
                                                userCardsActorsMap =
                                                    userCardsActorsMap.updated(userCreds.login, userCardsActor)

                                                ctx.complete(ApiStatusCode.OK)
                                            }
                                        }
                                    }


                                }
                            }
                        } ~
                        path("logout") {
                            post {
                                userSession { session =>
                                    myInvalidateSession { ctx =>
                                        if (!userCardsActorsMap.contains(session.username)) {
                                            logger.error(s"No user credentials actor available " +
                                                s"for user ${session.username}")
                                            ctx.reject() // =(
                                        } else {
                                            val cardsActor = userCardsActorsMap.get(session.username).get
                                            cardsActor ! "snap"
                                            cardsActor ! CardsActorEvent.Shutdown
                                            userCardsActorsMap = userCardsActorsMap - session.username
                                            // deleting user cards actor
                                            logger.info(s"Logging out $session")
                                            ctx.complete(ApiStatusCode.OK)
                                        }
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
                                        ctx.complete(ApiStatusCode.OK, session.username)
                                }
                            }
                        } ~
                        path("get_cards") {
                            get {
                                userSession { session => ctx =>
                                    ctx.complete(ApiStatusCode.OK)
                                }
                            }
                        } ~
                        path("add_new_card") {
                            post {
                                userSession { session =>
                                    entity(as[String]) { e =>
                                        val cardData = upickle.default.read[CardToRepeatData](e)
                                        logger.info(s"${session.username} wants to add new card: ${cardData}")

                                        if (!userCardsActorsMap.contains(session.username)) {
                                            logger.error("No user credentials actor available for logged user!")
                                            reject()
                                        } else {
                                            val cardsActor = userCardsActorsMap.get(session.username).get
                                            cardsActor ! CardsActorEvent.NewCard(cardData)
                                            complete(ApiStatusCode.OK)
                                        }
                                    }
                                }
                            }
                        } ~
                        path("delete_card") {
                            post {
                                userSession { session =>
                                    ctx =>
                                        ctx.complete(ApiStatusCode.OK)
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