package controllers

import connectors.{WemoConnector, NetatmoConnector}
import core.HumidityChecker
import play.api._
import play.api.mvc._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Application extends Controller {

  val wemoConnector = new WemoConnector
  val netatmoConnector = new NetatmoConnector
  val humidityChecker = new HumidityChecker(netatmoConnector.getHumidity _, wemoConnector) // The _ is to transform the method into a function

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