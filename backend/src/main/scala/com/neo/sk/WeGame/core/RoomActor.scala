package com.neo.sk.WeGame.core

import java.awt.event.KeyEvent

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import com.neo.sk.WeGame.brick.{GameProtocol, Protocol}
import com.neo.sk.WeGame.brickServer.GameServer
import com.neo.sk.WeGame.core.UserActor.JoinRoomSuccess
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.byteobject.ByteObject._
import org.slf4j.LoggerFactory
import com.neo.sk.WeGame.brick.GameConfig._
import com.neo.sk.WeGame.brick.Protocol.{KC, MP}
import com.neo.sk.WeGame._
import scala.concurrent.duration._
import scala.collection.mutable

object RoomActor {

  val log = LoggerFactory.getLogger(this.getClass)

  trait Command
  case object CompleteMsgFront extends Command
  case class FailMsgFront(ex: Throwable) extends Command


  private case object SyncTimeKey

  private case object Sync extends Command

  private case object TimeOutKey

  private case class ReliveTimeOutKey(id:String)

  private case object TimeOut extends Command

  case class JoinRoom(playerInfo: GameProtocol.playerInfo,roomId:Long,userActor:ActorRef[UserActor.Command]) extends Command

  case class KeyR(id:String, keyCode: Int,frame:Int) extends Command

  case class MouseR(id:String, clientX:Short,clientY:Short,frame:Int) extends Command

  def create(roomId:Long):Behavior[Command] = {
    log.debug(s"RoomActor-$roomId start...")
    Behaviors.setup[Command] { ctx =>
      Behaviors.withTimers[Command] {
        implicit timer =>
          val subscribersMap = mutable.HashMap[String,ActorRef[UserActor.Command]]()
          implicit val sendBuffer = new MiddleBufferInJvm(81920)
          /**每个房间都有一个自己的gird**/
          val grid = new GameServer()
          val playermap = mutable.HashMap[String,String]()
          grid.setRoomId(roomId)
          timer.startPeriodicTimer(SyncTimeKey, Sync, frameRate millis)
          idle(roomId, grid, playermap,0l)
      }
    }
  }

  def idle(
            roomId:Long,
            grid:GameServer,
            playerMap:mutable.HashMap[String,String], // [PlayId,nickName]  记录房间玩家数（包括等待复活） (仅人类，包括在玩及等待复活)
            tickCount:Long
          )(
            implicit timer:TimerScheduler[Command],
            sendBuffer:MiddleBufferInJvm
          ):Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case JoinRoom(playerInfo, roomId, userActor) =>
          playerMap.put(playerInfo.playerId,playerInfo.playerName)
          val position = if(playerMap.keys.size==1) 0 else 1
          userActor ! JoinRoomSuccess(roomId, ctx.self)
          grid.addPlayer(playerInfo.playerId, playerInfo.playerName,position)
          Behaviors.same

        case RoomActor.KeyR(id, keyCode,frame) =>
          Behaviors.same

        case RoomActor.MouseR(id,x,y,frame) =>
          Behaviors.same

        case Sync =>
          grid.update()
          Behaviors.same
      }
    }
  }

  def dispatch(subscribers:mutable.HashMap[String,ActorRef[UserActor.Command]])(msg:Protocol.GameMessage)(implicit sendBuffer:MiddleBufferInJvm) = {
    val isKillMsg = msg.isInstanceOf[Protocol.UserDeadMessage]
    subscribers.values.foreach( _ ! UserActor.DispatchMsg(Protocol.Wrap(msg.asInstanceOf[Protocol.GameMessage].fillMiddleBuffer(sendBuffer).result(),isKillMsg)))
  }

  def dispatchTo(subscribers:mutable.HashMap[String,ActorRef[UserActor.Command]])(id:String,msg:Protocol.GameMessage)(implicit sendBuffer:MiddleBufferInJvm) = {
    val isKillMsg = msg.isInstanceOf[Protocol.UserDeadMessage]
    subscribers.get(id).foreach( _ ! UserActor.DispatchMsg(Protocol.Wrap(msg.asInstanceOf[Protocol.GameMessage].fillMiddleBuffer(sendBuffer).result(),isKillMsg)))

  }

}
