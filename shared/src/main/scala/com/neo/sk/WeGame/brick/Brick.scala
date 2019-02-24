package com.neo.sk.WeGame.brick

import com.neo.sk.WeGame.brick.GameConfig.{Point, ball, brick, player}
import com.neo.sk.WeGame.brick.Protocol.{KC, MC}
import com.neo.sk.WeGame.brick.GameConfig._

import scala.math._
import scala.util.Random

trait Brick {
  def debug(msg: String): Unit

  def info(msg: String): Unit

  var GameField=Point(400,520)
  var minX=400
  var maxX=800
  var minY=40
  var maxY=560
  var myId = ""
  var roomId = 0l
  val random = new Random(System.nanoTime())
  var frameCount = 0

  var playerMap = Map.empty[String,player]
  var brickList = List[brick]() //(用户ID => 他的砖块）
  var ballList = List[ball]() //(用户ID => 他的球）

  var actionMap = Map.empty[Int, Map[String, KC]]

  var mouseActionMap = Map.empty[String, MC]

  var firstCome = true

  def update() = {
    updateBricks()
    updateBalls()
    actionMap -= frameCount
    frameCount += 1
  }

  def addBallMouseActionWithFrame(id:String, mc:MC) = {
    mouseActionMap += (id-> mc)
  }

  def updateBricks() = {
   checkCrash()
  }

  def updateBalls() = {
    val newPlayerMap = playerMap.values.map{ player =>
      val newPlayerBall = player.balls.map(ball =>{
        var newX = (ball.x + ball.targetX).toInt
        var newY = (ball.y + ball.targety).toInt
        var newspeedX = ball.targetX
        var newspeedY = ball.targety
        val mouseAct = mouseActionMap.get(player.id)
        if(mouseAct.isDefined){
            val mouse = mouseAct.get
            val deg = atan2(mouse.cY - ball.y, mouse.cX - ball.x)
            newspeedX = (cos(deg) * initBallSpeed).toFloat
            newspeedY = (sin(deg) * initBallSpeed).toFloat
        }
        /**边界碰撞，更新小球状态**/
        if(newX > maxX - ball.radius) {
          newX = maxX - ball.radius
          newspeedX = -ball.targetX
        }
        if(newX < minX ) {
          newX = minX
          newspeedX = -ball.targetX
        }
        val position=player.position
        if(position==1){ //上方
          if(newY <= minY) newY = 0
          if(newY > maxY - ball.radius) {
            newY=maxY-ball.radius
            newspeedY = -ball.targety
          }
        }else if(position==0){//下方
          if(newY < minY) {
            newY = minY
            newspeedY = -ball.targety
          }
          if(newY >= maxY-ball.radius) newY=0
        }
        ball.copy(x= newX,y = newY,targetX = newspeedX,targety = newspeedY)
      }).filterNot(i=>i.y==0)
      if(mouseActionMap.get(player.id).isDefined){
        mouseActionMap -= player.id
      }
      player.copy(balls = newPlayerBall)
    }
    //println(s"ballList:$ballList")
    ballList = newPlayerMap.map(s=>s.balls).toList.flatMap(i=>i.map(j=>j))
    playerMap = newPlayerMap.map(s=>(s.id,s)).toMap
  }

  def checkCrash()={
    checkBalltoBrick()
  }

  def checkBalltoBrick() = {
    val newPlayerMap = playerMap.values.map{
      player =>
        val newPlayerBall = player.balls.map(ball =>{
          var newX = (ball.x+ball.targetX).toInt
          var newY = (ball.y+ball.targety).toInt
          var newspeedX = ball.targetX
          var newspeedY = ball.targety
          val newBricklist=brickList.map { brick =>
            val brickx=brick.x
            val bricky=brick.y
            var newbrickCount=brick.count
            val distance=sqrt(pow(brickx-newX,2)+pow(bricky-newY,2))
            if(distance<brick.length){
              newspeedX = -newspeedX
              newspeedY = -newspeedY
              if(brick.id==player.id){
                newbrickCount -= 1
                println(s"brickcount:${brick.count} newbrickCount:$newbrickCount")
              }else if(brick.id!=player.id){
                newbrickCount += 1
              }
            }
            brick.copy(count=newbrickCount)
          }.filterNot(i=>i.count==0)
          brickList = newBricklist
          //player.copy(bricks=newBricklist)
          ball.copy(targetX = newspeedX, targety = newspeedY)
        })
        player.copy(balls = newPlayerBall)
    }
    ballList = newPlayerMap.map(s=>s.balls).toList.flatMap(i=>i.map(j=>j))
    playerMap = newPlayerMap.map(s=>(s.id, s.copy(bricks = brickList.filter(i=>i.id==s.id)))).toMap
  }




}
