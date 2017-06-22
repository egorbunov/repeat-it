# Repeat It App

This repo contains simple web application written for scala SPBAU course, in scala, using:

* Akka Persistence
* Akka HTTP
* Scala JS (using jquery bindings)


## Usage

The idea is simple: provide user with web interface to manage
and periodically repeat anything he (or she) wants to remember

For now u can:

* register / login / logout
* add new card (card is question on one side + answer on back side)
* see the whole list of added cards and click on them to see answers...

WARNING: it is implemented as one-page site, but without fancy frameworks, so u can't go previous
or next page for know, because site transitions are not stacked manually =)

## Run and Build

To Run: 

```bash
$ git clone https://github.com/egorbunov/repeat-it.git
$ cd repeat-it
$ sbt
$ sbt > appJVM/run
```

To create jar with all resource packed into:

```
$ sbt > package 
```


## Links

