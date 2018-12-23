package utils

import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

object UriUtil {
  def isValidUri(uri: Uri): Boolean = {
    val hostAddress = uri.authority.host.address

    uri.isAbsolute && hostsWhiteList.exists(whiteAddress => hostAddress.endsWith(whiteAddress))
  }

  private val hostsWhiteList = ConfigFactory.load().getStringList("play.filters.hosts.white-list").asScala.toList
}
