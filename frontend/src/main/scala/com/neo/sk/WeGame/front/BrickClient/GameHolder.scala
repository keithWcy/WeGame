package com.neo.sk.WeGame.front.BrickClient

import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.WeGame.brick.GameConfig.Point
import com.neo.sk.WeGame.brick.Protocol.{GameMessage, GridDataSync, MC}
import com.neo.sk.WeGame.front.common.Routes.GameRoute
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.{ErrorEvent, Event}
import com.neo.sk.WeGame.brick.GameConfig._
import com.neo.sk.WeGame.brick.Protocol
import org.scalajs.dom.ext.KeyCode

class GameHolder {
  var window = Point(1200,600)
  private[this] val GameCanvas = dom.document.getElementById("GameView").asInstanceOf[Canvas]
  private[this] val gameCtx = GameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private[this] val BpCanvas = dom.document.getElementById("backgroundView").asInstanceOf[Canvas]
  private[this] val BpCtx = GameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private[this] val InfoCanvas = dom.document.getElementById("InfoView").asInstanceOf[Canvas]
  private[this] val InfoCtx = GameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private[this] val drawGameView=DrawGame(gameCtx,GameCanvas,window)
  private[this] val drawBpView=DrawGame(BpCtx,BpCanvas,window)
  private[this] val drawInfo=DrawGame(InfoCtx,InfoCanvas,window)
  private[this] var gameState = 0 //0为等待，1为玩游戏，2为游戏结束

  val grid = new GameClient()

  //var myId = ""
  var nextInt = 0
  var nextFrame = 0
  var mouseInFlame = false
  var keyInFlame = false
  var mc = MC(None,0,0,0)
  private[this] var justSynced = false
  private[this] var logicFrameTime = System.currentTimeMillis()
  private[this] val actionSerialNumGenerator = new AtomicInteger(0)
  private[this] var syncGridData: scala.Option[GridDataSync] = None
  private[this] var firstCome=true
  private[this] var ballMove = true

  val webSocketClient = WebSocketClient(wsConnectSuccess,wsConnectError,wsMessageHandler,wsConnectClose)

  def getActionSerialNum=actionSerialNumGenerator.getAndIncrement()

  def init()={
    drawBpView.drawConnectWait()
  }

  def start(): Unit = {
    println("start---")
    /**
      * gameLoop: 150ms
      * gameRender: 约为16ms
      */
    nextInt=dom.window.setInterval(() => gameLoop, frameRate)
    dom.window.requestAnimationFrame(gameRender())
  }
  def update(): Unit = {
    grid.update()
  }

  def gameLoop: Unit = {
    logicFrameTime = System.currentTimeMillis()
    if (webSocketClient.getWsState) {
      //差不多每三秒同步一次
      //不同步
      if (!justSynced) {
        keyInFlame = false
        update()
      } else {
        if (syncGridData.nonEmpty) {
          //同步
          grid.setSyncGridData(syncGridData.get)
          syncGridData = None
        }
        justSynced = false
      }
    }
  }

  def gameRender(): Double => Unit = { d =>
    val curTime = System.currentTimeMillis()
    val offsetTime = curTime - logicFrameTime
    println(s"gameState:$gameState")
    gameState match {
      case 0 =>
        drawWait()
      case 1 =>
        draw(offsetTime)
      case 2 =>
        drawGameOver()
      case x =>
        println(s"gameState error:$gameState")

    }
    nextFrame = dom.window.requestAnimationFrame(gameRender())
  }

  def drawGameOver()={
    if(webSocketClient.getWsState){
      drawBpView.clearCanvas()
      drawInfo.clearCanvas()
      drawGameView.clearCanvas()
      val data=grid.getGridData(grid.myId)
      var myScore=0
      var oppScore=0
      data.playerDetails.find(_.id==grid.myId).get.bricks.foreach(i=>myScore+=i.count)
      data.playerDetails.find(_.id!=grid.myId) match{
        case Some(player) =>
          player.bricks.foreach(i=> oppScore +=i.count)
        case None =>
      }
      if(grid.playerMap.size==2){
        if(myScore>oppScore){
          drawInfo.drawResult(myScore,oppScore,false)
        }else if(myScore<oppScore){
          drawInfo.drawResult(myScore,oppScore,true)
        }else drawInfo.drawBalance(myScore,oppScore)
      }else drawInfo.drawOppLeave()
    }
  }

  def draw(offsetTime:Long)={
    if(webSocketClient.getWsState){
      val data=grid.getGridData(grid.myId)
      val myName=data.playerDetails.filter(_.id==grid.myId).map(_.name).head
      var othername="等待中"
      val other=data.playerDetails.filterNot(_.id==grid.myId).map(_.name)
      var myScore=0
      var oppScore=0
      data.playerDetails.find(_.id==grid.myId).get.bricks.foreach(i=>myScore+=i.count)
      data.playerDetails.find(_.id!=grid.myId) match{
        case Some(player) =>
          player.bricks.foreach(i=> oppScore +=i.count)
        case None =>
      }
      val position=data.playerDetails.find(_.id==grid.myId).get.position//1上，0下
      if(other.size==1) othername=other.head
      drawBpView.clearCanvas()
      drawBpView.drawBackGround()
      drawGameView.drawGrid(grid.myId,data,offsetTime,firstCome)
      drawGameView.drawWarning(position)
      drawInfo.drawRoomInfo(grid.roomId.toString,myName,othername,myScore,oppScore)
    }
    else{
      drawBpView.clearCanvas()
      drawBpView.drawGameLost()
    }
  }

  def drawWait()={
    drawBpView.clearCanvas()
    drawBpView.drawBackGround()
    drawBpView.drawGameWait()
  }

  def drawReady()={
    drawBpView.clearCanvas()
    drawBpView.drawBackGround()
    drawBpView.drawReady()
  }

  def joinGame(playerId: String,
               playerName:String,
               roomId: Long
              ): Unit = {
    val url = GameRoute.getWebSocketUri(dom.document,playerId,playerName,roomId)
    //开启websocket
    webSocketClient.setUp(url)
    start()//gameloop + gamerender
    //用户行为：使用键盘or鼠标(观战模式不响应键盘鼠标事件）
    addActionListenEvent
  }

  def addActionListenEvent = {
    InfoCanvas.focus()
    //在画布上监听键盘事件
    InfoCanvas.onkeydown = {
      (e: dom.KeyboardEvent) => {
                println(s"keydown: ${e.keyCode} ${gameState} ")
        if (keyInFlame == false) {
          if (gameState == 0) {
            if (e.keyCode == KeyCode.Space) {
              gameState = 1
              addMouseListenEvent()
              keyInFlame = true
            }
          }
        }
      }
    }
  }

  def addMouseListenEvent()={
    InfoCanvas.onclick = { (e: dom.MouseEvent) =>
      //球在木板上时选择方向发射
      println("mouse click")
      if(grid.playerMap.get(grid.myId).isDefined && ballMove){
        val pageX = e.pageX
        val pageY = e.pageY
        println(s"pageX:$pageX pageY:$pageY")
        mc = MC(None, pageX.toShort, pageY.toShort, grid.frameCount)
        grid.addBallMouseActionWithFrame(grid.myId, mc)
        webSocketClient.sendMsg(mc)
        //ballMove = false
      }
    }
  }



  private def wsConnectSuccess(e:Event) = {
    println(s"连接服务器成功")
    e
  }

  private def wsConnectError(e:ErrorEvent) = {
    println("----wsConnectError")
    e
  }
  private def wsMessageHandler(data:GameMessage):Unit = {
    data match{
      case Protocol.Id(id) =>
        grid.myId = id

      case m:Protocol.MC =>
        if(m.id.isDefined){
          val mid = m.id.get
          if(!grid.myId.equals(mid)){
            grid.addBallMouseActionWithFrame(mid,m)
          }
        }

      case Protocol.PlayerJoin(id,player) =>
        if(!grid.playerMap.contains(player.id)) {
          grid.playerMap += (player.id -> player)
        }

      case data: Protocol.GridDataSync =>
        println("获取全量数据  get ALL GRID===================")
        syncGridData = Some(data)
        justSynced = true

      case Protocol.RoomId(id) =>
        grid.roomId = id

      case Protocol.PlayerLeft(id) =>
        if(grid.playerMap.get(id).isDefined){
          grid.removePlayer(id)
          if(id == grid.myId)
            gameClose
          else {
            gameState = 2
          }
        }

      case x=>
    }
  }

  private def wsConnectClose(e:Event) = {
    println("last Ws close")
    e
  }

  def gameClose={
    webSocketClient.closeWs
    dom.window.cancelAnimationFrame(nextFrame)
    dom.window.clearInterval(nextInt)
  }
}

