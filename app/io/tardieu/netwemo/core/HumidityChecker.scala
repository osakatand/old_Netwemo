package io.tardieu.netwemo.core

import java.util.concurrent.TimeUnit

import akka.actor.Cancellable
import com.typesafe.config.ConfigFactory
import connectors.WemoConnector
import org.joda.time.LocalTime
import com.github.nscala_time.time.Imports._
import play.api.Logger

import play.api.libs.concurrent.Akka.system
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Class responsible for continuously checking Netatmo metrics in order
 * to update the switches.
 */
class HumidityChecker(getValueFunction: () => Future[Float], wemoConnector: WemoConnector) {

  val conf = ConfigFactory.load.getConfig("humidity")

  // Those values should be read from the database
  private var startTime: LocalTime = new LocalTime(conf.getInt("startHour"), conf.getInt("stopMinute"))
  private var stopTime: LocalTime = new LocalTime(conf.getInt("stopHour"), conf.getInt("stopMinute"))
  private var lowThreshold = conf.getInt("lowThreshold")
  private var highThreshold = conf.getInt("highThreshold")
  private var checkInterval = FiniteDuration(10, TimeUnit.MINUTES)

  private var schedule: Cancellable = _

  private def inService(startTime: LocalTime, stopTime: LocalTime): Boolean = {
    val now = LocalTime.now()
    val b = (now > startTime) && (now < stopTime)
    Logger.debug(s"Humidity in service: $b")
    b
  }

  /**
   * Not implemented yet.
   * Reads values from database and updates class variables.
   */
  def updateValues(): Unit = {
    startTime = startTime
    stopTime = stopTime
    lowThreshold = lowThreshold
    highThreshold = highThreshold
    checkInterval = checkInterval
  }

  schedule = system.scheduler.schedule(FiniteDuration(0, TimeUnit.MINUTES), checkInterval)(runCheck(getValueFunction))

  /**
   * Checks and switches the switch on or off
   * @param getValue the function providing the metric value
   */
  def runCheck(getValue: () => Future[Float]): Unit = {
    if (inService(startTime, stopTime)) {
      val futureValue = getValue()
      futureValue.foreach { value =>
        if (value > highThreshold) {
          Logger.debug(s"Humidity: $value % > high threshold $highThreshold, switching on.")
          wemoConnector.switchOn("desu")
        }
        if (value < lowThreshold) {
          Logger.debug(s"Humidity: $value % < low threshold $lowThreshold, switching off.")
          wemoConnector.switchOff("desu")
        }
      }
    }
    else {
      Logger.debug(s"Quiet hours, switching off humidity")
      wemoConnector.switchOff("desu")
    }
  }


}
