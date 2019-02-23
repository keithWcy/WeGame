package com.neo.sk.WeGame.core

import com.neo.sk.WeGame.brick.Protocol.{KC, MP}
import com.neo.sk.WeGame.brick.{GameProtocol, Protocol}
import com.neo.sk.WeGame.Boot.roomManager
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.stream.OverflowStrategy
import org.slf4j.LoggerFactory
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import org.seekloud.byteobject.ByteObject._
import org.seekloud.byteobject.MiddleBufferInJvm
import scala.concurrent.duration._
import scala.language.implicitConversions

object UserActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  private final case object BehaviorChangeKey
  private final val InitTime = Some(5.minutes)

  case class WebSocketMsg(reqOpt: Option[Protocol.UserAction]) extends Command
  case class DispatchMsg(msg:Protocol.WsMsgSource) extends Command

  case class StartGame(roomId:Option[Long]) extends Command
  case class TimeOut(msg: String) extends Command
  case class Key(keyCode: Int,frame:Int) extends Command
  case class Mouse(clientX:Short,clientY:Short,frame:Int) extends Command
  case class JoinRoom(playerInfo: GameProtocol.playerInfo,roomIdOpt:Option[Long] = None,userActor:ActorRef[UserActor.Command]) extends Command with RoomManager.Command
  case class JoinRoomSuccess(roomId:Long, roomActor: ActorRef[RoomActor.Command]) extends Command with RoomManager.Command
  case class UserFrontActor(actor: ActorRef[Protocol.WsMsgSource]) extends Command
  case class Left(playerInfo: GameProtocol.playerInfo) extends Command with RoomActor.Command
  case object CompleteMsgFront extends Command
  case class FailMsgFront(ex: Throwable) extends Command
  case class UserLeft[U](actorRef: ActorRef[U]) extends Command

  private case object UnKnowAction extends Command


  private[this] def switchBehavior(ctx: ActorContext[Command],
                                   behaviorName: String,
                                   behavior: Behavior[Command],
                                   durationOpt: Option[FiniteDuration] = None,
                                   timeOut: TimeOut  = TimeOut("busy time error")
                                  )(
                                    implicit stashBuffer: StashBuffer[Command],
                                    timer:TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path}  becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey,timeOut,_))
    stashBuffer.unstashAll(ctx,behavior)
  }

  private def sink(actor: ActorRef[Command],recordId:Long) = ActorSink.actorRef[Command](
    ref = actor,
    onCompleteMessage = CompleteMsgFront,
    onFailureMessage = FailMsgFront.apply
  )


  def flow(id:String,name:String,recordId:Long,actor:ActorRef[UserActor.Command]):Flow[WebSocketMsg, Protocol.WsMsgSource,Any] = {
    val in = Flow[UserActor.WebSocketMsg]
      .map {a=>
        val req = a.reqOpt.get
        req match{
          case KC(_,keyCode,f)=>
            log.debug(s"键盘事件$keyCode")
            Key(keyCode,f)

          case MP(_,clientX,clientY,f)=>
            Mouse(clientX,clientY,f)


          case Protocol.JoinRoom(roomIdOp) =>
            log.info("JoinRoom!!!!!!")
            StartGame(roomIdOp)

          case _=>
            UnKnowAction
        }
      }
      .to(sink(actor,recordId))

    val out =
      ActorSource.actorRef[Protocol.WsMsgSource](
        completionMatcher = {
          case Protocol.CompleteMsgServer ⇒
        },
        failureMatcher = {
          case Protocol.FailMsgServer(e)  ⇒ e
        },
        bufferSize = 128,
        overflowStrategy = OverflowStrategy.dropHead
      ).mapMaterializedValue(outActor => actor ! UserFrontActor(outActor))
    Flow.fromSinkAndSource(in, out)
  }

  def create(playerInfo:GameProtocol.playerInfo): Behavior[Command] = {Behaviors.setup[Command]{ctx =>
    log.debug(s"${ctx.self.path} is starting...")
    implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
    Behaviors.withTimers[Command]{ implicit timer =>
      implicit val sendBuffer = new MiddleBufferInJvm(8192)
      switchBehavior(ctx,"init",init(playerInfo),InitTime,TimeOut("init"))
    }
  }
  }

  private def init(userInfo:GameProtocol.playerInfo)(
    implicit stashBuffer:StashBuffer[Command],
    sendBuffer:MiddleBufferInJvm,
    timer:TimerScheduler[Command]
  ): Behavior[Command] =
    Behaviors.receive[Command]{(ctx, msg) =>
      msg match {
        case UserFrontActor(frontActor) =>
          ctx.watchWith(frontActor,UserLeft(frontActor))
          switchBehavior(ctx,"idle", idle(userInfo,System.currentTimeMillis(),frontActor))

        case UserLeft(actor) =>
          ctx.unwatch(actor)
          Behaviors.stopped


        case TimeOut(m) =>
          log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
          Behaviors.stopped

        case unknowMsg =>
          stashBuffer.stash(unknowMsg)
          Behavior.same
      }

    }

  private def idle(
                    userInfo:GameProtocol.playerInfo,
                    startTime:Long,
                    frontActor: ActorRef[Protocol.WsMsgSource]
                  )(
                    implicit stashBuffer:StashBuffer[Command],
                    sendBuffer:MiddleBufferInJvm,
                    timer:TimerScheduler[Command]
                  ):Behavior[Command] =
    Behaviors.receive[Command] {(ctx,msg) =>
      msg match {
        case StartGame(roomIdOp) =>
          roomManager ! UserActor.JoinRoom(userInfo,roomIdOp,ctx.self)
          Behaviors.same

        case UserLeft(actor) =>
          ctx.unwatch(actor)
          switchBehavior(ctx,"init",init(userInfo),InitTime,TimeOut("init"))

        case JoinRoomSuccess(roomId,roomActor)=>
          frontActor ! Protocol.Wrap(Protocol.JoinRoomSuccess(userInfo.playerId,roomId).asInstanceOf[Protocol.GameMessage].fillMiddleBuffer(sendBuffer).result())
          //          frontActor ! Protocol.JoinRoomSuccess(userInfo.playerId,roomId)
          switchBehavior(ctx,"play",play(userInfo,frontActor,roomActor))


        case UnKnowAction =>
          Behavior.same


        case unknowMsg=>
          stashBuffer.stash(unknowMsg)
          Behavior.same
      }
    }

  private def play(
                    userInfo:GameProtocol.playerInfo,
                    frontActor:ActorRef[Protocol.WsMsgSource],
                    roomActor: ActorRef[RoomActor.Command])(
                    implicit stashBuffer:StashBuffer[Command],
                    timer:TimerScheduler[Command],
                    sendBuffer:MiddleBufferInJvm
                  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case Key(keyCode,frame) =>
          log.debug(s"got $msg")
          roomActor ! RoomActor.KeyR(userInfo.playerId, keyCode,frame)
          Behaviors.same

        case Mouse(x,y,frame) =>
          log.debug(s"gor $msg")
          roomActor ! RoomActor.MouseR(userInfo.playerId,x,y,frame)
          Behaviors.same

        case DispatchMsg(m)=>
          //          log.info(s"bot:    $m")
          frontActor ! m
          Behaviors.same


        case UserLeft(actor) =>
          ctx.unwatch(actor)
          println("actor has died,so user left")
          roomManager ! RoomManager.LeftRoom(userInfo)
          Behaviors.stopped

        case unKnowMsg =>
          stashBuffer.stash(unKnowMsg)
          Behavior.same
      }
    }


}
