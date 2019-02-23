package com.neo.sk.WeGame.front.BrickClient

import com.neo.sk.WeGame.brick.GameConfig.Point
import com.neo.sk.WeGame.brick.Protocol.GridDataSync
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLElement

case class DrawGame(ctx:CanvasRenderingContext2D,
                    canvas:Canvas,
                    size:Point
                   ) {

  val width=size.x
  val height=size.y
  this.canvas.width= size.x
  this.canvas.height= size.y

  private[this] val ballImg=dom.document.getElementById("ball").asInstanceOf[HTMLElement]

  def drawConnectWait()={
    ctx.fillStyle = Color.Black.toString()
    ctx.font = "36px Helvetica"
    ctx.fillText("正在等待游戏连接", 150, 180)
  }

  def drawGameWait()={
    val txt="已加入房间，按空格准备"
    ctx.fillStyle = "rgba(99, 99, 99, 1)"
    ctx.font = "24px Helvetica"
    ctx.fillText(txt,width/2-200,height/2-30)
  }

  def drawReady()={
    val txt="已准备，等待其他玩家"
    ctx.fillStyle = "rgba(99, 99, 99, 1)"
    ctx.font = "24px Helvetica"
    ctx.fillText(txt,width/2-200,height/2-30)
  }
  def drawGameLost(): Unit = {
    ctx.fillStyle = "rgba(99, 99, 99, 1)"
    ctx.font = "36px Helvetica"
    ctx.fillText("Ops, connection lost....", 350, 250)
  }

  def drawBackGround()={
    ctx.strokeStyle=Color.Black.toString()
    ctx.strokeRect(width/2-200,height/2-260,400,520)
    ctx.fillStyle="rgb(240,248,255)"
    ctx.fillRect(width/2-200,height/2-260,400,520)
  }

  def clearCanvas()={
    ctx.fillStyle = Color.White.toString()
    ctx.fillRect(0, 0, this.canvas.width , this.canvas.height )
  }

  def drawRoomInfo(roomId:String,myName:String,otherName:String)={
    ctx.fillStyle = Color.Black.toString()
    ctx.font= "24px Helvetica"
    ctx.fillText(s"房间号：$roomId",300,100)
    ctx.fillText(s"Your Name:$myName",300,150)
    ctx.fillText(s"Oppo Name:$otherName",300,200)
  }

  def drawGrid(uid:String,data:GridDataSync,OffsetTime:Long,firstCome:Boolean)={
    clearCanvas()
    val bricks=data.brickDetails
    val balls=data.ballDetails
    ctx.strokeStyle=Color.Black.toString()
    ctx.strokeRect(width/2-200,height/2-260,400,520)
    ctx.fillStyle="rgb(240,248,255)"
    ctx.fillRect(width/2-200,height/2-260,400,520)
    bricks.groupBy(_.id).foreach{
      i=>
        println(s"uid:$uid brickid:${i._1}")
        if(i._1==uid) {
        ctx.fillStyle = Color.Blue.toString()
        i._2.foreach { i =>
          ctx.font = "18px Helvetica"
          ctx.fillText(i.count.toString,i.x, i.y)
          ctx.strokeStyle = Color.Blue.toString()
          ctx.strokeRect(i.x, i.y, i.length, i.length)
        }
      }
        else {
          ctx.fillStyle = Color.Red.toString()
          i._2.foreach { i =>
            ctx.font = "18px Helvetica"
            ctx.fillText(i.count.toString,i.x, i.y)
            ctx.strokeStyle = Color.Red.toString()
          ctx.strokeRect(i.x,i.y,i.length,i.length)
        }
      }
    }

    balls.foreach{ball=>
      if(ball.IsDraw){
        ctx.drawImage(ballImg,ball.x,ball.y,ball.radius,ball.radius)
      }
    }
  }

}
