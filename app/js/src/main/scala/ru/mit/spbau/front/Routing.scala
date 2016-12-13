package ru.mit.spbau.front

import org.scalajs.dom.ext.Ajax
import org.scalajs.jquery.jQuery

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


object Routing {
    /**
      * Fills page and runs given continuation ater ready
      *
      * @param placeholderPageLink page to get and fill placeholder
      * @param setupper setup page function
      * @param placeholderId id of html element where to put contents
      */
    def switchPageContents(placeholderPageLink: String,
                           setupper: () => Unit,
                           placeholderId: String): Unit = {
        Ajax.get(
            url = placeholderPageLink
        ).onSuccess({ case xhr =>
            jQuery(s"#$placeholderId").html(xhr.responseText)
            setupper()
        })
    }
}
