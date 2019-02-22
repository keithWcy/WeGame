package com.neo.sk.WeGame.front.BrickClient

import com.neo.sk.WeGame.brick.Brick
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

}
