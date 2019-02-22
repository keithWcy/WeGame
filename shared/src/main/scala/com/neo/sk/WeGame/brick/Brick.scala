package com.neo.sk.WeGame.brick

import com.neo.sk.WeGame.brick.GameConfig.{ball, brick, player}
import com.neo.sk.WeGame.brick.Protocol.{KC, MP}

import scala.util.Random

trait Brick {
  def debug(msg: String): Unit

  def info(msg: String): Unit

  var myId = ""
  val random = new Random(System.nanoTime())
  var frameCount = 0

  var playerMap = Map.empty[String,player]
  var brickList = List[brick]() //(用户ID => 他的砖块）
  var ballList = List[ball]() //(用户ID => 他的球）

  var actionMap = Map.empty[Int, Map[String, KC]]

  var mouseActionMap = Map.empty[Int, Map[String, MP]]

  var firstCome = true

  def update() = {
    updateBricks()
    updateBalls()
    actionMap -= frameCount
    mouseActionMap -= frameCount
    frameCount += 1
  }

  private[this] def updateBricks() = {
    checkCrash()
    brickList = brickList.map{mass=>
      mass
    }
  }
  private[this] def updateBalls()={

  }


  def checkCrash()={
    checkBalltoBound()
    checkBalltoBrick()
    checkBalltoOver()
  }

  def checkBalltoBound()={

  }

  def checkBalltoBrick()={

  }

  def checkBalltoOver()={

  }


  def getGridData(id:String) = {
    myId = id
    var playerDetails: List[player] = Nil
    var brickDetails:List[brick] = Nil
    var ballDetails:List[ball] = Nil
    playerMap.foreach{
      case (_,player) =>
          playerDetails ::= player

    }
    brickList.foreach{
      case brick =>
       brickDetails::=brick
    }
    ballList.foreach{
      case ball =>
        ballDetails::=ball
    }
    Protocol.GridDataSync(
      frameCount,
      playerDetails,
      brickDetails,
      ballDetails
    )
  }


}
