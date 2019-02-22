package com.neo.sk.WeGame.http

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.server.Route
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{parameter, path, pathEndOrSingleSlash, pathPrefix}
import akka.stream.Materializer
import akka.util.Timeout
import akka.actor.typed.scaladsl.AskPattern._
import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow
import com.neo.sk.WeGame.core.UserManager
import com.neo.sk.WeGame.Boot.userManager
import com.neo.sk.WeGame.brick.GameProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}

trait gameService extends ServiceUtils{

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val scheduler: Scheduler

  implicit val timeout: Timeout

  private def playGame = (path("playGame") & get & pathEndOrSingleSlash) {
    parameter(
      'playerId.as[String],
      'playerName.as[String],
      'roomId.as[Long].?
    ) { case (playerId, playerName, roomIdOpt) =>
      val flowFuture:Future[Flow[Message,Message,Any]]= userManager ? (UserManager.GetWebSocketFlow(GameProtocol.playerInfo(playerId,playerName),roomIdOpt,_))
        dealFutureResult(
          flowFuture.map{r=>
            handleWebSocketMessages(r)
          }
        )
    }
  }

  val gameRoute:Route = pathPrefix("game") {
    playGame
  }

}
