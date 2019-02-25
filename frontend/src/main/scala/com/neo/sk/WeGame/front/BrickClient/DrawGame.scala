package com.neo.sk.WeGame.front.BrickClient

import com.neo.sk.WeGame.brick.GameConfig.Point
import com.neo.sk.WeGame.brick.Protocol.GridDataSync
import com.neo.sk.WeGame.front.utils.ShortCut
import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, MouseEvent}
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLElement
import com.neo.sk.WeGame.brick.GameConfig._

case class DrawGame(ctx:CanvasRenderingContext2D,
                    canvas:Canvas,
                    size:Point
                   ) {

  val width=size.x
  val height=size.y
  this.canvas.width= dom.window.innerWidth.toInt
  this.canvas.height= dom.window.innerHeight.toInt

  private[this] val myImg=dom.document.getElementById("ball").asInstanceOf[HTMLElement]
  private[this] val OpImg=dom.document.getElementById("basket").asInstanceOf[HTMLElement]
  private[this] val SeaImg=dom.document.getElementById("sea").asInstanceOf[HTMLElement]
  private[this] val SkyImg=dom.document.getElementById("sky").asInstanceOf[HTMLElement]
  private[this] val WallImg=dom.document.getElementById("wall").asInstanceOf[HTMLElement]
  private[this] val rePlayButton=dom.document.getElementById("rePlay").asInstanceOf[HTMLElement]
  rePlayButton.onclick={e: MouseEvent =>
    val playerId=System.currentTimeMillis().toString
    val playerName="游客"
    val url=s"brick/playGame?playerId=$playerId&playerName=$playerName"
    ShortCut.redirect(url)
  }
  val numOff=Point(12,24)
  def drawConnectWait()={
    ctx.fillStyle = Color.Black.toString()
    ctx.font = "36px Helvetica"
    ctx.fillText("正在等待游戏连接", 150, 180)
  }

  def drawGameWait()={
    val txt="已加入房间，按空格准备"
    ctx.fillStyle = "rgba(99, 99, 99, 1)"
    ctx.font = "24px Helvetica"
    ctx.fillText(txt,width/2-150,height/2-30)
  }

  def drawReady()={
    val txt="已准备，等待其他玩家"
    ctx.fillStyle = "rgba(99, 99, 99, 1)"
    ctx.font = "24px Helvetica"
    ctx.fillText(txt,width/2-150,height/2-30)
  }
  def drawGameLost(): Unit = {
    ctx.fillStyle = "rgba(99, 99, 99, 1)"
    ctx.font = "36px Helvetica"
    ctx.fillText("Ops, connection lost....", 350, 250)
  }

  def drawWarning(position:Int)={
    ctx.fillStyle=Color.Red.toString()
    if(position==1){
      ctx.fillRect(width/2-200,height/2-260,400,5)
    }else if(position==0){
      ctx.fillRect(width/2-200,height/2-260+515,400,5)
    }
  }

  def drawBackGround()={
    ctx.drawImage(SeaImg,0,0,this.canvas.width,this.canvas.height)
    ctx.strokeStyle=Color.Black.toString()
    ctx.strokeRect(width/2-200,height/2-260,400,520)
    ctx.fillStyle="#fbf0b6"
    ctx.fillRect(width/2-200,height/2-260,400,520)
  }

  def clearCanvas() = {
    ctx.clearRect(0,0,this.canvas.width,this.canvas.height)
  }

  def drawResult(myScore:Int,oppScore:Int,isWinner:Boolean)={
    ctx.fillStyle=Color.Black.toString()
    ctx.font="22px Helvetica"
    if(isWinner) {
      ctx.fillText("YOU WIN!!",300,100)
      ctx.fillText(s"结算:获取${math.abs(oppScore-myScore)}分",300,310)
    }
    else {
      ctx.fillText("YOU FAIL",300,100)
      ctx.fillText(s"结算:扣除${math.abs(myScore-oppScore)}分",300,310)
    }
    ctx.fillText(s"你的砖块数:$myScore",300,170)
    ctx.fillText(s"对方砖块数:$oppScore",300,240)
    rePlayButton.style.display="inline"

  }

  def drawBalance(myScore:Int,oppScore:Int)={
    ctx.fillStyle=Color.Black.toString()
    ctx.font="22px Helvetica"
    ctx.fillText("平局～～",300,100)
    ctx.fillText(s"你的砖块数:$myScore",300,170)
    ctx.fillText(s"对方砖块数:$oppScore",300,240)
    rePlayButton.style.display="inline"
  }

  def drawOppLeave()={
    ctx.fillStyle=Color.Black.toString()
    ctx.font="22px Helvetica"
    ctx.fillText("你真厉害！对手已逃离！",300,200)
    rePlayButton.style.display="inline"
  }

  def drawRoomInfo(roomId:String,myName:String,otherName:String,myScore:Int,oppScore:Int)={
    ctx.fillStyle = Color.Black.toString()
    ctx.font= "18px Helvetica"
    ctx.fillText(s"房间号：$roomId",70,100)
    ctx.fillText(s"Your :$myName  $myScore",70,150)
    ctx.fillText(s"Oppo:$otherName  $oppScore",70,200)
  }

  def drawGrid(uid:String,data:GridDataSync,OffsetTime:Long,firstCome:Boolean)={
    clearCanvas()
    val bricks=data.brickDetails
    val balls=data.ballDetails
//    ctx.strokeStyle=Color.Black.toString()
//    ctx.strokeRect(width/2-200,height/2-260,400,520)
//    ctx.fillStyle="rgb(240,248,255)"
//    ctx.fillRect(width/2-200,height/2-260,400,520)
    ctx.font = "18px Helvetica"
    bricks.groupBy(_.id).foreach{
      i=>
        if(i._1==uid) {
        i._2.foreach { i =>
//          ctx.strokeStyle = Color.Blue.toString()
//          ctx.strokeRect(i.x, i.y, i.length, i.length)
          ctx.fillStyle = "#47c1f5"
          ctx.fillRect(i.x,i.y,i.length,i.length)
          ctx.fillStyle = "#fffdfa"
          ctx.fillText(i.count.toString,i.x+numOff.x, i.y+numOff.y)
        }
      }
        else {
          i._2.foreach { i =>
//            ctx.strokeStyle = Color.Red.toString()
//          ctx.strokeRect(i.x,i.y,i.length,i.length)
            ctx.fillStyle = "#fb6a73"
            ctx.fillRect(i.x,i.y,i.length,i.length)
            ctx.fillStyle = "#fffdfa"
            ctx.fillText(i.count.toString,i.x+numOff.x, i.y+numOff.y)
        }
      }
    }

    balls.foreach{ball=>
      val x=ball.x+ball.targetX * OffsetTime.toFloat/frameRate
      val y=ball.y+ball.targety * OffsetTime.toFloat/frameRate
      if(ball.IsDraw){
        if(ball.id==uid){
          ctx.drawImage(myImg,x,y,ball.radius,ball.radius)
        }else ctx.drawImage(OpImg,x,y,ball.radius,ball.radius)
      }
    }
  }

  var timeNum=60
  var clock1=0

  def drawClock():Unit={
    clock1 = dom.window.setInterval(()=>clock(timeNum),1000)
  }

  def clock(time:Int):Unit={
    ctx.fillStyle = Color.White.toString()
    ctx.fillRect(0, 0, this.canvas.width , this.canvas.height )
    ctx.fillStyle = "rgba(99, 19, 99, 1)"
    ctx.font = "36px Helvetica"
    ctx.fillText(s"游戏剩余时间:${time}s", 300, 380)
    timeNum = timeNum-1
  }

}
