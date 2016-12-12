package com.couchmate.jscookie

import scala.scalajs.js

@js.native
object Cookies extends js.Object {
    def get(): String = js.native

    def get(name: String): js.UndefOr[String] = js.native

    def getJSON(): js.Any = js.native

    def getJSON(name: String): js.UndefOr[js.Any] = js.native

    def set(name: String, value: String): Unit = js.native

    def set(name: String, value: String, opts: CookieOpts): Unit = js.native

    def remove(name: String, path: Path): Unit = js.native
}
