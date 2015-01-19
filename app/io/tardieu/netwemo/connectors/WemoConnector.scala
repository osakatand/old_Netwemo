package connectors

import play.api.libs.ws.{WS, WSResponse}
import play.api.Play.current

import scala.concurrent.Future

/**
 * Handles the connection to Wemo device.
 * Uses the REST API of Ouimeaux (http://ouimeaux.readthedocs.org/) to control the switch.
 * The Ouimeaux server must run in the same network than the switch.
 */
class WemoConnector {

  def switchOn(): Unit = switchState("on")
  def switchOff(): Unit = switchState("off")
  def toggle(): Unit = switchState("toggle")

  private def switchState(state: String): Unit = {
    val futureResponse: Future[WSResponse] =
      WS.url("http://localhost:5000/api/device/desu")
        .withQueryString("state" -> state)
        .post(Map[String, Seq[String]]())
    // TODO: Check the response to catch errors
  }

}
