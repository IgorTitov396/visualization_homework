package controllers

import akka.http.scaladsl.model.Uri
import forms.SiteForm._
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.mvc._
import services.GraphBuilderService
import utils.UriUtil

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class MainPageController @Inject()(
  cc: MessagesControllerComponents,
  graphBuilderService: GraphBuilderService
)(implicit assetsFinder: AssetsFinder, ex: ExecutionContext) extends MessagesAbstractController(cc) {

  private val postUrl = routes.MainPageController.showGraphPage()

  def showGraphPage = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      Ok(views.html.index(form, postUrl, "Invalid data fields")).async
    }

    val successFunction = { data: Data =>
      val uriOpt = Try(Uri(data.webAddress)).toOption
      val depthOpt = Try(data.graphDepth.toInt).toOption

      (uriOpt, depthOpt) match {
        case (None, _) => Ok(views.html.index(form, postUrl, "Invalid url")).async
        case (Some(uri), _) if !uri.isAbsolute => Ok(views.html.index(form, postUrl, "Uri is not absolute")).async
        case (Some(uri), _) if !UriUtil.isValidUri(uri) => Ok(views.html.index(form, postUrl, s"""Host is not supported. Supported hosts: [".wikipedia.org"]""")).async
        case (_, None) => Ok(views.html.index(form, postUrl, s"Depth should be a number")).async
        case (_, Some(depth)) if depth <= 0 => Ok(views.html.index(form, postUrl, s"Depth should be > 0")).async
        case (_, Some(depth)) if depth > maxDepth => Ok(views.html.index(form, postUrl, s"Depth value is too large. Max supported depth: $maxDepth")).async
        case (Some(uri), Some(depth)) => graphBuilderService.buildGraph(uri, depth).map {
          case Some(graph) => Ok(views.html.graph(graph))
          case None => Ok(views.html.index(form, postUrl, s"Can't get data from $uri"))
        }
      }
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def showMainPage = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(form, postUrl))
  }

  implicit class ResultEnrichment(result: Result) {
    def async = Future.successful(result)
  }

  private val maxDepth = 3
}
