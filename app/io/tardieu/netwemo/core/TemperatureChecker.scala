package io.tardieu.netwemo.core

import java.util.concurrent.TimeUnit

import akka.actor.Cancellable
import com.typesafe.config.ConfigFactory
import connectors.WemoConnector
import org.joda.time.LocalTime
import play.api.Logger
import play.api.libs.concurrent.Akka._
import com.github.nscala_time.time.Imports._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Class responsible for continuously checking Netatmo metrics in order
 * to update the switches.
 */
class TemperatureChecker(getValueFunction: () => Future[Float], wemoConnector: WemoConnector) {

  val conf = ConfigFactory.load.getConfig("temperature")

  // Those values should be read from the database
  private var lowThreshold = conf.getDouble("lowThreshold")
  private var highThreshold = conf.getDouble("highThreshold")
  private var coldLowThreshold = conf.getDouble("coldLowThreshold")
  private var coldHighThreshold = conf.getDouble("coldHighThreshold")
  private var coldStartTime: LocalTime = new LocalTime(conf.getInt("coldHourStart"), conf.getInt("coldMinuteStart"))
  private var coldStopTime: LocalTime = new LocalTime(conf.getInt("coldHourStop"), conf.getInt("coldMinuteStop"))
  private var checkInterval = FiniteDuration(10, TimeUnit.MINUTES)

  private var schedule: Cancellable = _

  schedule = system.scheduler.schedule(FiniteDuration(1, TimeUnit.MINUTES), checkInterval)(runCheck(getValueFunction))

  private def isWorkingDay: Boolean = {
    LocalDateTime.now.getDayOfWeek match {
      case 6 | 7 => false
      case _ => true
    }
  }

  private def inColdHours(startTime: LocalTime, stopTime: LocalTime): Boolean = {
    val now = LocalTime.now()
    val b = (now > startTime) && (now < stopTime) && (isWorkingDay)
    Logger.debug(s"In cold hours: $b")
    b
  }

  /**
   * Checks and switches the switch on or off
   * @param getValue the function providing the metric value
   */
  def runCheck(getValue: () => Future[Float]): Unit = {
    val futureValue = getValue()
    inColdHours(coldStartTime, coldStopTime) match {
      case true => setState(futureValue, coldLowThreshold, coldHighThreshold)
      case false => setState(futureValue, lowThreshold, highThreshold)
    }
  }

  private def setState(futureValue: Future[Float], lowThreshold: Double, highThreshold: Double): Unit = {
    futureValue.foreach { value =>
      if (value > highThreshold) {
        Logger.debug(s"Temperature: $value > high threshold $highThreshold, switching off.")
        wemoConnector.switchOff("chauffage")
      }
      if (value < lowThreshold) {
        Logger.debug(s"Temperature: $value < low threshold $lowThreshold, switching on.")
        wemoConnector.switchOn("chauffage")
      }
    }
  }

}

