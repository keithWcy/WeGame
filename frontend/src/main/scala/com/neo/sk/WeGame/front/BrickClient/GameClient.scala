package com.neo.sk.WeGame.front.BrickClient

import com.neo.sk.WeGame.brick.{Brick, Protocol}
import com.neo.sk.WeGame.brick.Protocol.GridDataSync
import com.neo.sk.WeGame.brick.GameConfig._

class GameClient extends Brick{

  override def debug(msg: String): Unit = println(msg)

  override def info(msg: String): Unit = println(msg)

  def setSyncGridData(data:GridDataSync): Unit = {
    actionMap = actionMap.filterKeys(_ > data.frameCount- maxDelayFrame)
    mouseActionMap = mouseActionMap.filterKeys(_ > data.frameCount-maxDelayFrame)
    frameCount = data.frameCount
    playerMap = data.playerDetails.map(s => s.id -> s).toMap
    brickList = data.brickDetails
    ballList = data.ballDetails
  }


  def getGridData(id:String) = {
    myId = id
    var playerDetails: List[player] = Nil
    //    var brickDetails:List[brick] = Nil
    //    var ballDetails:List[ball] = Nil
    playerMap.foreach{
      case (_,player) =>
        playerDetails ::= player

    }
    Protocol.GridDataSync(
      frameCount,
      playerDetails,
      brickList,
      ballList
    )
  }

}
