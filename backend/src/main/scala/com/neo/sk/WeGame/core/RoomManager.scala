package com.neo.sk.WeGame.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import com.neo.sk.WeGame.brick.GameProtocol
import com.neo.sk.WeGame.core.UserActor.JoinRoom
import org.slf4j.LoggerFactory

import scala.collection.mutable

object RoomManager {

  private val log=LoggerFactory.getLogger(this.getClass)
  trait Command
  case object TimeKey
  case object TimeOut extends Command
  case class LeftRoom(playerInfo: GameProtocol.playerInfo) extends Command

  val behaviors:Behavior[Command] ={
    log.debug(s"UserManager start...")
    Behaviors.setup[Command]{
      ctx =>
        Behaviors.withTimers[Command]{
          implicit timer =>
            val roomIdGenerator = new AtomicLong(1L)
            val roomInUse = mutable.HashMap((1l,List.empty[(String,String)]))
            idle(roomIdGenerator,roomInUse)
        }
    }
  }

  def idle(roomIdGenerator:AtomicLong,roomInUse:mutable.HashMap[Long,List[(String,String)]])(implicit timer:TimerScheduler[Command])=
    Behaviors.receive[Command]{
      (ctx,msg)=>
        msg match {
          case JoinRoom(playerInfo,roomIdOpt,userActor) =>
            roomIdOpt match{
              case Some(roomId) =>
 /*               roomInUse.get(roomId) match{
                  case Some(ls) =>
                    val num=ls.length
                    if(num<2)
                    roomInUse.put(roomId,(playerInfo.playerId,playerInfo.playerName) :: ls)
                    getRoomActor(ctx,roomId) ! RoomActor.JoinRoom(playerInfo,roomId,userActor)
                  case None =>
                    roomInUse.put(roomId,List((playerInfo.playerId,playerInfo.playerName)))
                }*/
              case None =>
                val num=roomInUse.get(roomIdGenerator.get) match {
                  case Some(users) =>
                    users.length
                  case None =>
                    0
                }
                if(num<2){
                  var roomId=roomIdGenerator.get()
                  roomInUse.get(roomId) match{
                    case Some(user) =>
                      roomInUse.put(roomId,(playerInfo.playerId,playerInfo.playerName) :: user)
                    case None =>
                      roomInUse.put(roomId,List((playerInfo.playerId,playerInfo.playerName)))
                  }
                  getRoomActor(ctx,roomId) ! RoomActor.JoinRoom(playerInfo,roomId,userActor)
                }
                else{
                  var roomId=roomIdGenerator.incrementAndGet()
                  roomInUse.put(roomId,List((playerInfo.playerId,playerInfo.playerName)))
                  getRoomActor(ctx,roomId) ! RoomActor.JoinRoom(playerInfo,roomId,userActor)
                }
            }
            log.debug(s"now roomInUse:$roomInUse")
            Behaviors.same


          case LeftRoom(playerInfo) =>
            roomInUse.find(_._2.exists(_._1 == playerInfo.playerId)) match{
              case Some(t) =>
                roomInUse.put(t._1,t._2.filterNot(_._1 == playerInfo.playerId))
                getRoomActor(ctx,t._1) ! UserActor.Left(playerInfo)
                if(roomInUse(t._1).isEmpty && t._1 > 1l)roomInUse.remove(t._1)
              case None => log.debug(s"该玩家不在任何房间")
            }
            Behaviors.same


          case x=>
            log.debug(s"msg can't handle with ${x}")
            Behaviors.unhandled
        }
    }


  private def getRoomActor(ctx: ActorContext[Command],roomId:Long):ActorRef[RoomActor.Command] = {
    val childName = s"RoomActor-$roomId"
    ctx.child(childName).getOrElse{
      ctx.spawn(RoomActor.create(roomId),childName)
    }.upcast[RoomActor.Command]
  }
}
