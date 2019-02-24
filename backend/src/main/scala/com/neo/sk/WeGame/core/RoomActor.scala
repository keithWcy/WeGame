package com.neo.sk.WeGame.core

import com.neo.sk.WeGame.brick.{GameProtocol, Protocol}
import com.neo.sk.WeGame.brickServer.GameServer
import com.neo.sk.WeGame.core.UserActor.JoinRoomSuccess
import com.neo.sk.WeGame.brick.GameConfig._

import scala.language.postfixOps
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.scaladsl.Behaviors
import com.neo.sk.WeGame.brick.Protocol.MC
import org.slf4j.LoggerFactory
import org.seekloud.byteobject.ByteObject._
import org.seekloud.byteobject.MiddleBufferInJvm

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
          idle(roomId, grid, playermap,subscribersMap,0l)
      }
    }
  }

  def idle(
            roomId:Long,
            grid:GameServer,
            playerMap:mutable.HashMap[String,String], // [PlayId,nickName]  记录房间玩家数（包括等待复活） (仅人类，包括在玩及等待复活)
            subscribersMap:mutable.HashMap[String,ActorRef[UserActor.Command]],
            tickCount:Long
          )(
            implicit timer:TimerScheduler[Command],
            sendBuffer:MiddleBufferInJvm
          ):Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case JoinRoom(playerInfo, roomId, userActor) =>
          playerMap.put(playerInfo.playerId,playerInfo.playerName)
          subscribersMap.put(playerInfo.playerId, userActor)
          val position = if(playerMap.keys.size==1) 0 else 1 //0表示第一个加入，1表示第二个加入
          userActor ! JoinRoomSuccess(roomId, ctx.self)
          grid.addPlayer(playerInfo.playerId, playerInfo.playerName,position)
          dispatchTo(subscribersMap)(playerInfo.playerId, Protocol.Id(playerInfo.playerId))
          dispatchTo(subscribersMap)(playerInfo.playerId,Protocol.RoomId(roomId))
          Behaviors.same

        case RoomActor.KeyR(id, keyCode,frame) =>
          Behaviors.same


        case RoomActor.MouseR(id,x,y,frame) =>
          if(grid.playerMap.get(id).isDefined){
            grid.addBallMouseActionWithFrame(id,MC(Some(id),x,y,math.max(grid.frameCount,frame)))
            dispatch(subscribersMap)(MC(Some(id),x,y,math.max(grid.frameCount,frame)))
          }
          Behaviors.same

        case Sync =>
          grid.getSubscribersMap(subscribersMap)
          grid.update()
          if(tickCount % 20 == 0){
            val gridData = grid.getAllGridData
            dispatch(subscribersMap)(gridData)
          }
          idle(roomId, grid, playerMap, subscribersMap, tickCount + 1)

        case x =>
          Behaviors.unhandled
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
