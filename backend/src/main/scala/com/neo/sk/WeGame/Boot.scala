package com.neo.sk.WeGame

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.neo.sk.WeGame.core.{RoomManager, UserManager}
import com.neo.sk.WeGame.http.HttpService
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import scala.language.postfixOps
import akka.actor.typed.scaladsl.adapter._

/**
  * User: Taoz
  * Date: 8/26/2016
  * Time: 10:25 PM
  */
object Boot extends HttpService {

  import concurrent.duration._
  import com.neo.sk.WeGame.common.AppSettings._


  override implicit val system = ActorSystem("WeGame", config)
  // the executor should not be the default dispatcher.
  override implicit val executor = system.dispatchers.lookup("akka.actor.my-blocking-dispatcher")
  override implicit val materializer = ActorMaterializer()
  override implicit val scheduler = system.scheduler

  val roomManager: ActorRef[RoomManager.Command] =system.spawn(RoomManager.behaviors,"roomManager")
  val userManager:ActorRef[UserManager.Command] = system.spawn(UserManager.create(),"userManager")

  override val timeout = Timeout(20 seconds) // for actor asks

  val log: LoggingAdapter = Logging(system, getClass)



  def main(args: Array[String]) {
    log.info("Starting.")
    Http().bindAndHandle(routes, httpInterface, httpPort)
    log.info(s"Listen to the $httpInterface:$httpPort")
    log.info("Done.")
  }






}
