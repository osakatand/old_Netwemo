package core

import java.util.concurrent.TimeUnit

import akka.actor.Cancellable
import connectors.WemoConnector
import org.joda.time.LocalTime
import com.github.nscala_time.time.Imports._

import play.api.libs.concurrent.Akka.system
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Class responsible for continuously checking Netatmo metrics in order
 * to update the switches.
 */
class Checker(getValueMethod: () => Future[Long], wemoConnector: WemoConnector) {


  // Those values should be read from the database
  private var startTime: LocalTime = new LocalTime(2, 0) // 2 hours 0 minutes
  private var stopTime: LocalTime = new LocalTime(20, 30) // 20 hours 30 minutes
  private var lowThreshold = 50
  private var highThreshold = 55
  private var checkInterval = FiniteDuration(10, TimeUnit.MINUTES)

  private var schedule: Cancellable = _

  private def inService(startTime: LocalTime, stopTime: LocalTime): Boolean = {
    val now = LocalTime.now()
    (now > startTime) && (now < stopTime)
  }

  /**
   * Not implemented yet.
   * Reads values from database and updating class variables.
   */
  def updateValues(): Unit = {
    startTime = startTime
    stopTime = stopTime
    lowThreshold = lowThreshold
    highThreshold = highThreshold
    checkInterval = checkInterval
  }

  schedule = system.scheduler.schedule(FiniteDuration(0, TimeUnit.MINUTES), checkInterval)(runCheck(getValueMethod))

  /**
   * Checks and switches the switch on or off
   * @param getValue the function providing the metric value
   */
  def runCheck(getValue: () => Future[Long]): Unit = {
    if (inService(startTime, stopTime)) {
      val futureValue = getValue()
      futureValue.foreach { value =>
        if (value > highThreshold) {
          wemoConnector.switchOn()
        }
        if (value > lowThreshold) {
          wemoConnector.switchOff()
        }
      }
    }
  }


}
