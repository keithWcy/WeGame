package com.neo.sk.WeGame.front.common

import org.scalajs.dom
import org.scalajs.dom.html.Document


object Routes {

  object GameRoute{
    private val baseUrl = "/WeGame/brick"

    private def playGame(playerId:String,
                         playerName:String,
                         roomId:Long
                        ) = {
      if(roomId == 0l)
        baseUrl + s"/playGame?playerId=$playerId&playerName=$playerName"
      else
        baseUrl + s"/playGame?playerId=$playerId&playerName=$playerName&roomId=$roomId"

    }

    def getWebSocketUri(document: Document,
                        playerId:String,
                        playerName:String,
                        roomId:Long
                       ):String = {
      val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
      val wsUrl = playGame(playerId,playerName,roomId)
      s"$wsProtocol://${dom.document.location.host}$wsUrl"
    }
  }


}