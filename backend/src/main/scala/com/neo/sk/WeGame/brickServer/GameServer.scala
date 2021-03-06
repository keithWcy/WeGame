package com.neo.sk.WeGame.brickServer

import akka.actor.typed.ActorRef
import com.neo.sk.WeGame.brick.GameConfig.{ball, brick, player}
import com.neo.sk.WeGame.brick.Protocol.{Id, PlayerJoin}
import com.neo.sk.WeGame.brick.{Brick, GameConfig, GameProtocol, Protocol}
import com.neo.sk.WeGame.core.UserActor
import org.seekloud.byteobject.MiddleBufferInJvm

import scala.collection.mutable
import scala.math.sqrt
import scala.util.Random
import com.neo.sk.WeGame.core.RoomActor._

class GameServer extends Brick {

  override def debug(msg: String): Unit = println(msg)

  override def info(msg: String): Unit = println(msg)

  private[this] var waitingJoin = Map.empty[String, (String,Int)]
  private [this] var subscriber=mutable.HashMap[String,ActorRef[UserActor.Command]]()
  implicit val sendBuffer = new MiddleBufferInJvm(81920)

  def setRoomId(id:Long)={
    roomId = id
  }

  private[this] def genWaitingStar() = {
    waitingJoin.filterNot(kv => playerMap.contains(kv._1)).foreach { case (id, (name,position)) =>
      var x=0
      var y=0
      var bricklist:List[brick]=Nil
      var balllist:List[ball]=Nil
      if(position==1){ //上方
        x=600
        y=20
        for(j <- 0 to 3){
          for(i <- 1 to 2){
            val num= new Random(System.nanoTime()).nextInt(8)
            val brickCount = new Random(System.nanoTime()).nextInt(4)+1
            bricklist ::= brick(id,400+num*37,300-(j+1)*37,count=brickCount)
          }
        }
        for(i <- 0 to 2)
        balllist ::= ball(id,x-10+(i-1)*20,y+22,0,0)
      }else{
        x=600
        y=580
        for(j <- 0 to 3){
          for(i <- 1 to 2){
            val num= new Random(System.nanoTime()).nextInt(8)
            val brickCount = new Random(System.nanoTime()).nextInt(4)+1
            bricklist ::= brick(id,400+num*37,300+j*37,count=brickCount)
          }
        }
        for(i <- 0 to 2)
          balllist ::= ball(id,x-10+(i-1)*20,y-42,0,0)
      }
      brickList = bricklist ::: brickList
      ballList = balllist ::: ballList
      val player = GameConfig.player(id, name, position,x,y,bricklist,balllist)
      playerMap += id -> player
      dispatch(subscriber)(getAllGridData())
      dispatch(subscriber)(PlayerJoin(id,player))
    }

    waitingJoin = Map.empty[String, (String,Int)]
  }

  def addPlayer(id: String, name: String,position:Int) = waitingJoin += (id -> (name,position))


  override def update(): Unit = {
    super.update()
    genWaitingStar()  //新增
  }

  def getSubscribersMap(subscribersMap:mutable.HashMap[String,ActorRef[UserActor.Command]]) ={
    subscriber=subscribersMap
  }


  def getAllGridData() = {
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
