package controllers

import java.util.Date

import akka.actor.{Props, Actor}
import akka.contrib.throttle.TimerBasedThrottler
import akka.contrib.throttle.Throttler._
import play.api.libs.iteratee.Concurrent.Channel

import play.api.libs.iteratee.{Concurrent, Enumerator}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.concurrent.Akka
import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  val system = Akka.system(Play.current)


  def test() = Action {

    val printer = system.actorOf(Props[PrinterActor])

    val throttler = system.actorOf(Props(classOf[TimerBasedThrottler], 3 msgsPer 1.second))
    throttler ! SetTarget(Some(printer))

    val channel: Enumerator[String] = Concurrent.unicast(onStart = channel => {
        throttler  ! channel
        (1 to 100).map( x => throttler ! s"Message $x")
        throttler ! Enumerator.eof
      }
    )

    Ok.chunked(channel).as("text/html")

  }
}

class PrinterActor extends Actor {
  import context._

  var output: Option[Channel[String]] = None

  def active: Receive = {
    case x : String =>
      output.get.push(new Date() + " : "+x+"<br/>")
    case end  =>
      output.get.end()
      output = None
      become(inactive)
  }

  def inactive: Receive = {
    case channel : Channel[String] =>
      output = Some(channel)
      become(active)

  }

  def receive  = inactive
}