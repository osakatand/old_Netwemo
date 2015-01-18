package controllers

import connectors.NetatmoConnector
import play.api._
import play.api.mvc._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Application extends Controller {

  val netatmoConnector = new NetatmoConnector

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getHumidity = Action {
    val response = Await.result(netatmoConnector.getHumidity, 5 seconds)
    Ok(response.toString)
  }

  def getTemperature = Action {
    val response = Await.result(netatmoConnector.getTemperature, 5 seconds)
    Ok(response.toString)
  }

  def refresh = Action {
    val response = Await.result(netatmoConnector.refreshToken, 5 seconds).body
    Ok(response)
  }

}