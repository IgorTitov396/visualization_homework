package scalatags

import scalatags.Text.all._
import scalacss.ScalatagsCss._

object JsObjects {

  def searchButton = {
    html(
      head(
        script(src:="..."),
        script(
          "alert('Hello World')"
        )
      ),
      body(
        div(
          h1(id:="title", "This is a title"),
          p("This is a big paragraph of text")
        )
      )
    )
  }

}
