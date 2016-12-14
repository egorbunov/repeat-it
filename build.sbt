name := "repeat-it-webapp"
scalaVersion in ThisBuild := "2.11.8"
version := "1.0"

lazy val root = project.in(file(".")).
    aggregate(appJS, appJVM).
    settings(
        publish := {},
        publishLocal := {}
    )

val app = crossProject.in(file("app")).
    settings(
        libraryDependencies ++= Seq(
            "com.lihaoyi" %%% "upickle" % "0.4.3"
        ),
        scalaVersion := "2.11.8"
    ).
    jsSettings(
        libraryDependencies ++= Seq(
            "org.scala-js" %%% "scalajs-dom" % "0.9.1",
            "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
            "org.webjars" % "js-cookie" % "2.1.0",
            "org.scala-lang.modules" %% "scala-async" % "0.9.6",
            "com.lihaoyi" %%% "scalatags" % "0.6.1"
        ),
        jsDependencies ++= Seq(
            "org.webjars" % "jquery" % "2.1.3" / "2.1.3/jquery.js",
            "org.webjars" % "js-cookie" % "2.1.0" / "js.cookie.js"
        )
    ).
    jvmSettings(
        libraryDependencies ++= Seq(
            "com.typesafe.akka" %% "akka-persistence" % "2.4.14",
            "org.iq80.leveldb" % "leveldb" % "0.7",
            "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
            "com.typesafe.akka" %% "akka-http" % "10.0.0",
            "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
            "com.softwaremill.akka-http-session" %% "core" % "0.3.0",
            "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
            "ch.qos.logback" % "logback-classic" % "1.1.7"
        )
    )

lazy val appJS = app.js
lazy val appJVM = app.jvm.settings(
    (resources in Compile) += (fastOptJS in (appJS, Compile)).value.data,
    (resources in Compile) += (packageJSDependencies in (appJS, Compile)).value
//    (resources in Compile) += (fullOptJS in (appJS, Compile)).value.data,
//    (resources in Compile) += (packageMinifiedJSDependencies in (appJS, Compile)).value
)
