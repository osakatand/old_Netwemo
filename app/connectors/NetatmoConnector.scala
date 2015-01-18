package connectors

import com.typesafe.config.ConfigFactory
import play.api.libs.ws.{WSResponse, WS, WSRequestHolder}
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Object containing the different metrics available
 */
object Metric extends Enumeration {
  type MetricType = Value
  val Humidity, Temperature = Value
}

/**
 * Handles connection to Netatmo REST API.
 */
class NetatmoConnector {

  private val conf = ConfigFactory.load

  private val client_id = conf.getString("client_id")
  private val client_secret = conf.getString("client_secret")
  private var refresh_token = conf.getString("refresh_token")
  private val device_id = conf.getString("device_id")

  private var access_token = "54bb73f11b7759a9638e2314|9171bbe847fdb4836fde29cf22f748da"

  private val postRefreshParameters = Map(
  "grant_type" -> Seq("refresh_token"),
  "refresh_token" -> Seq(refresh_token),
  "client_id" -> Seq(client_id),
  "client_secret" -> Seq(client_secret)
  )


  def refreshToken: Future[WSResponse] = {

    val futureResponse: Future[WSResponse] =
      WS.url("http://api.netatmo.net/oauth2/token")
        .withHeaders("Content-type" -> "application/x-www-form-urlencoded")
        .post(postRefreshParameters)

    futureResponse
  }

  def getMetric(metricType: Metric.MetricType) = {
    val futureResponse =
      WS.url("http://api.netatmo.net/api/getmeasure")
        .withQueryString("access_token" -> access_token,
        "device_id" -> device_id,
        "scale" -> "max",
        "date_end" -> "last",
        "type" -> metricType.toString)
        .get


    futureResponse.map {
      response =>
        ((response.json \ "body")(0) \ "value")(0)(0).as[Float]
    }
  }

  def getHumidity = getMetric(Metric.Humidity)
  def getTemperature = getMetric(Metric.Temperature)

}
