package com.neo.sk.WeGame.brick

import com.neo.sk.WeGame.brick.GameConfig.{ball, brick, player}

object Protocol {
  trait WsMsgSource

  case object CompleteMsgServer extends WsMsgSource
  case class FailMsgServer(ex: Throwable) extends WsMsgSource
  case class Wrap(ws:Array[Byte],isKillMsg:Boolean = false) extends WsMsgSource

  sealed trait WsSendMsg
  sealed trait GameMessage extends WsSendMsg
  sealed trait UserAction extends WsSendMsg

  case class JoinRoom(roomId:Option[Long]) extends UserAction
  case class ErrorWsMsgFront(msg:String) extends GameMessage
  case class UserDeadMessage(deadId:String, score:Short) extends GameMessage
  case class Id(id: String) extends GameMessage
  case class RoomId(id:Long) extends GameMessage
  case class MC(id: Option[String],cX:Short,cY:Short,frame:Int) extends UserAction with GameMessage
  case class KC(id: Option[String],keyCode:Short,frame:Int) extends UserAction with GameMessage
  case class JoinRoomSuccess(playerId:String, roomId:Long) extends GameMessage
  case class PlayerJoin(id:String, player:player) extends GameMessage //id: 映射id
  case class PlayerLeft(id: String) extends GameMessage
  case class gameOver(roomId:Long) extends GameMessage

  case class GridDataSync(
                           frameCount: Int,
                           playerDetails: List[player],
                           brickDetails:List[brick],
                           ballDetails:List[ball]
                         ) extends GameMessage
}
