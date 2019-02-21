package com.neo.sk.WeGame.brick

object Protocol {
  trait WsSendMsg
  sealed trait GameMessage extends WsSendMsg
  sealed trait UserAction extends WsSendMsg

  case class ErrorWsMsgFront(msg:String) extends GameMessage
  case class MP(id: Option[Byte],cX:Short,cY:Short) extends UserAction with GameMessage

}
