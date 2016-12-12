package com.couchmate.jscookie

import scala.scalajs.js

@js.native
trait Path extends js.Object {
    val path: String = js.native
}

object Path {
    def apply(path: String = "/"): Path = {
        js.Dynamic.literal(
            path = path
        ).asInstanceOf[Path]
    }
}
