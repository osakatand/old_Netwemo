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
    Ok(io.tardieu.netwemo.views.html.index("Your new application is ready."))
  }

  def getHumidity = Action.async {
    netatmoConnector.getHumidity.map {
      humidity => Ok(humidity.toString)
    }
  }

  def getTemperature = Action.async {
    netatmoConnector.getTemperature.map {
      temperature => Ok(temperature.toString)
    }
  }

  def refresh = Action {
    Ok(netatmoConnector.refreshToken)
  }

}