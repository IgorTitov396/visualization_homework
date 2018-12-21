package controllers

import forms.SiteForm._
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.twirl.api.Html
import scalatags.JsObjects

@Singleton
class MainPageController @Inject()(
  cc: MessagesControllerComponents,
  ws: WSClient
)(implicit assetsFinder: AssetsFinder) extends MessagesAbstractController(cc) {

  private val postUrl = routes.MainPageController.showMainPage()

  def showMainPage = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(form, postUrl))
  }
}
