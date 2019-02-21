package com.neo.sk.WeGame.brick

object Protocol {
  trait WsSendMsg
  sealed trait GameMessage extends WsSendMsg
  sealed trait UserAction extends WsSendMsg

}
