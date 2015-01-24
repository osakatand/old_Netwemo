package io.tardieu.netwemo.core

import java.util.concurrent.TimeUnit

import akka.actor.Cancellable
import connectors.WemoConnector
import play.api.Logger
import play.api.libs.concurrent.Akka._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Class responsible for continuously checking Netatmo metrics in order
 * to update the switches.
 */
class TemperatureChecker(getValueFunction: () => Future[Float], wemoConnector: WemoConnector) {

  // Those values should be read from the database
  private var lowThreshold = 16
  private var highThreshold = 17
  private var checkInterval = FiniteDuration(10, TimeUnit.MINUTES)

  private var schedule: Cancellable = _

  /**
   * Not implemented yet.
   * Reads values from database and updates class variables.
   */
  def updateValues(): Unit = {
    lowThreshold = lowThreshold
    highThreshold = highThreshold
    checkInterval = checkInterval
  }

  schedule = system.scheduler.schedule(FiniteDuration(1, TimeUnit.MINUTES), checkInterval)(runCheck(getValueFunction))

  /**
   * Checks and switches the switch on or off
   * @param getValue the function providing the metric value
   */
  def runCheck(getValue: () => Future[Float]): Unit = {
      val futureValue = getValue()
      futureValue.foreach { value =>
        if (value > highThreshold) {
          Logger.debug(s"Temperature: $value % > high threshold $highThreshold, switching off.")
          wemoConnector.switchOn("chauffage")
        }
        if (value < lowThreshold) {
          Logger.debug(s"Temperature: $value % < low threshold $lowThreshold, switching on.")
          wemoConnector.switchOff("chauffage")
        }
    }
  }
}

