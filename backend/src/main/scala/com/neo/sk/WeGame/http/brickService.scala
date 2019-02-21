package com.neo.sk.WeGame.http

import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{getFromResource, handleWebSocketMessages, parameter, path, pathEndOrSingleSlash, pathPrefix}
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.server.Directives._

trait brickService {

  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  val brickRoute={
    (pathPrefix("brick") & get) {
      pathEndOrSingleSlash {
        getFromResource("html/index.html")

      } ~path("welCome") {
        {
          redirect(s"/WeGame/brick#/HiBrick",
            StatusCodes.SeeOther
          )
        }
      }~ path("playGame") {
        parameter(
          'playerId.as[String],
          'playerName.as[String],
          'roomId.as[Long].?
        ){
          case (playerId, playerName,roomIdOpt) =>
            redirect(s"/WeGame/brick#/playGame/${playerId}/${URLEncoder.encode(playerName,"utf-8")}/${roomIdOpt.getOrElse(0l)}",
              StatusCodes.SeeOther
            )
        }
      }
    }
  }
}

