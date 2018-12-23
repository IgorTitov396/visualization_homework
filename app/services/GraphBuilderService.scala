package services

import akka.http.scaladsl.model.{StatusCodes, Uri}
import javax.inject._
import models.Graph
import play.api.libs.ws.{WSClient, WSResponse}
import akka.http.scaladsl.model.StatusCode._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import play.api.Logger
import utils.UriUtil

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait GraphBuilderService {
  def buildGraph(url: Uri, maxDepth: Int): Future[Option[Graph]]
}

@Singleton
class GraphBuilderServiceImpl @Inject() (
  ws: WSClient
)(implicit ex: ExecutionContext) extends GraphBuilderService {
  type FOG = Future[Option[Graph]]

  override def buildGraph(url: Uri, maxDepth: Int): Future[Option[Graph]] = {
    buildGraphReq(url, maxDepth)
  }

  private def buildGraphReq(url: Uri, maxDepth: Int, currentDepth: Int = 0): Future[Option[Graph]] = {
    if (currentDepth < maxDepth) {
      ws.url(url.toString()).withRequestTimeout(requestTimeout).get().flatMap { response =>
        val statusCode = int2StatusCode(response.status)
        val html = browser.parseString(response.body)
        //Logger.info(s"Status code of answer: $statusCode")
        //Logger.warn(s"Response body:\n ${response.body}")
        val name = getName(html).getOrElse("Unknown")
        statusCode match {
          case StatusCodes.OK =>
            val uriList = getReferenceUrls(html, url)
            val filteredUriList = uriList.filter(UriUtil.isValidUri)

            for {
              childrenGrpahList <- Future.traverse(filteredUriList)(buildGraphReq(_, maxDepth, currentDepth + 1)).map(_.flatten)
            } yield Some(Graph(name, childrenGrpahList, url.toString()))
          case _ if statusCode.isRedirection() && locationHeaderExists(response) =>
            val location = response.header("Location").get
            val redirectUriOpt = createUri(location, url)
            redirectUriOpt.fold[FOG](emptyFutureOpt)(buildGraphReq(_, currentDepth))
          case _ => emptyFutureOpt
        }
      }.recover {
        case ex =>
          Logger.error(s"Exception during GET $url", ex)
          None
      }
    } else {
      emptyFutureOpt
    }
  }.andThen {
    case res => Logger.info(s"RESULT: $res")
  }

  private def getName(doc: Browser#DocumentType): Option[String] = {
    Try(doc >> text("#firstHeading")).toOption
  }

  private def getReferenceUrls(doc: Browser#DocumentType, baseUri: Uri): List[Uri] = {

    val preparedHtml = (doc >> elementList("#mw-content-text"))
      .map(_.outerHtml)
      .mkString

    hrefRegex.findAllIn(preparedHtml)
      .matchData
      .map(_.group(2))
      .filter(_.startsWith("/wiki/"))
      .filter(!_.contains(":"))
      .map(_.replaceAll("#.*", ""))
      .flatMap(createUri(_, baseUri))
      .toList
  }


  private def createUri(uriString: String, baseUri: Uri) = {
    Try {
      val uri = Uri(uriString)
      if (!uri.isAbsolute) {
        uri.resolvedAgainst(baseUri)
      } else uri
    }.recover {
      case ex =>
        Logger.info(s"Unable to create absolute URL from base=$baseUri, relative=$uriString")
        throw ex
    }.toOption

  }

  private def locationHeaderExists(response: WSResponse): Boolean = {
    response.header("Location").isDefined
  }

  private val browser = JsoupBrowser()
  private val emptyFutureOpt = Future.successful(Option.empty)
  private val hrefRegex = ".<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1.*".r
  private val requestTimeout = 5.second
}
