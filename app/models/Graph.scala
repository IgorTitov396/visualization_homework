package models

import play.api.libs.json.{Json, OFormat}

case class Graph(name: String, children: Seq[Graph], link: String)

object Graph {
  implicit lazy val graphFormat: OFormat[Graph] = Json.format[Graph]
}