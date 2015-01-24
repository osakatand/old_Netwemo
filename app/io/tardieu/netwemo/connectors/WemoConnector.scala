package connectors

import play.api.Logger
import play.api.libs.ws.{WS, WSResponse}
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Handles the connection to Wemo device.
 * Uses the REST API of Ouimeaux (http://ouimeaux.readthedocs.org/) to control the switch.
 * The Ouimeaux server must run in the same network than the switch.
 */
class WemoConnector {

  def switchOn(device: String): Unit = switchState("on", device)
  def switchOff(device: String): Unit = switchState("off", device)
  def toggle(device: String): Unit = switchState("toggle", device)

  private def switchState(state: String, device: String): Unit = {
    val futureResponse: Future[WSResponse] =
      WS.url(s"http://localhost:5000/api/device/$device")
        .withQueryString("state" -> state)
        .post(Map[String, Seq[String]]())
    // TODO: Check the response to catch errors
    futureResponse.map { r =>
      Logger.debug(s"Switch order received with status ${r.status} ${r.statusText}: ${r.body}")
    }.recover {
      case e: Throwable => Logger.error("Problem talking to ouimeaux: ", e)
    }
  }

}
