package ru.mit.spbau.front

import org.scalajs.dom.ext.Ajax
import org.scalajs.jquery.jQuery

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


object Commons {
    // http response status codes
    val OK = 200

    /**
      * Fills page and runs given continuation ater ready
      * @param placeholderPageLink page to get and fill placeholder
      * @param setupper setup page function
      */
    def switchToPage(placeholderPageLink: String, setupper: () => Unit): Unit = {
        Ajax.get(
            url = placeholderPageLink
        ).onSuccess({ case xhr =>
            jQuery("#placeholder").html(xhr.responseText)
            setupper()
        })
    }
}
