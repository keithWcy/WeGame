package com.neo.sk.WeGame.brickServer

import com.neo.sk.WeGame.brick.GameConfig.{ball, brick}
import com.neo.sk.WeGame.brick.{Brick, GameConfig, GameProtocol}

import scala.math.sqrt
import scala.util.Random

class GameServer extends Brick {

  override def debug(msg: String): Unit = println(msg)

  override def info(msg: String): Unit = println(msg)

  private var roomId = 0l
  private[this] var waitingJoin = Map.empty[String, (String,Int)]

  def setRoomId(id:Long)={
    roomId = id
  }

  private[this] def genWaitingStar() = {
    waitingJoin.filterNot(kv => playerMap.contains(kv._1)).foreach { case (id, (name,position)) =>
      var x=0
      var y=0
      var bricklist:List[brick] = Nil
      var balllist:List[ball] = Nil
      if(position==1){ //上方
        x=600
        y=20
        for(j <- 0 to 1){
          for(i <- 1 to 2){
            val num= new Random(System.nanoTime()).nextInt(8)
            bricklist ::= brick(id,400+num*35,300-(j+1)*30)
          }
        }
        for(i <- 1 to 3)
        balllist ::= ball(id,600,20,0,0)
      }else{
        x=600
        y=580
        for(j <- 0 to 1){
          for(i <- 1 to 2){
            val num= new Random(System.nanoTime()).nextInt(8)
            bricklist ::= brick(id,400+num*35,300-(j+1)*30)
          }
        }
        for(i <- 1 to 3)
          balllist ::= ball(id,600,580,0,0)
      }
      val player = GameConfig.player(id, name, position,x,y,brickList,ballList)
      playerMap += id -> player
      brickList ::: bricklist
      ballList ::: balllist
    }
    waitingJoin = Map.empty[String, (String,Int)]
  }

  def addPlayer(id: String, name: String,position:Int) = waitingJoin += (id -> (name,position))


  override def update(): Unit = {
    super.update()
    genWaitingStar()  //新增
  }


}
