package controllers

import forms.SiteForm._
import javax.inject.{Inject, Singleton}
import models.Graph
import play.api.data.Form
import play.api.libs.ws.WSClient
import play.api.mvc._

@Singleton
class MainPageController @Inject()(
  cc: MessagesControllerComponents,
  ws: WSClient
)(implicit assetsFinder: AssetsFinder) extends MessagesAbstractController(cc) {

  private val postUrl = routes.MainPageController.showGraphPage()

  def showGraphPage = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      Ok(views.html.graph(Graph("Hi2", Seq(Graph("hi", Seq.empty)))))
    }

    val successFunction = { data: Data =>
      Ok(views.html.graph(Graph(data.graphDepth, Seq(Graph("hi", Seq.empty)))))
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def showMainPage = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(form, postUrl))
  }
}
