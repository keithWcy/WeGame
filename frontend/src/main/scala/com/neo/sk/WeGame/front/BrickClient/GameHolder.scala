package com.neo.sk.WeGame.front.BrickClient

import com.neo.sk.WeGame.brick.Protocol.GameMessage
import com.neo.sk.WeGame.front.common.Routes.GameRoute
import com.neo.sk.WeGame.snake.Point
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.{ErrorEvent, Event}

class GameHolder {
  var window = Point(1200, 600)
  private[this] val GameCanvas = dom.document.getElementById("GameView").asInstanceOf[Canvas]
  private[this] val gameCtx = GameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private[this] val BpCanvas = dom.document.getElementById("backgroundView").asInstanceOf[Canvas]
  private[this] val BpCtx = GameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private[this] val drawGameView=DrawGame(gameCtx,GameCanvas,window)
  private[this] val drawBpView=DrawGame(BpCtx,BpCanvas,window)

  val webSocketClient = WebSocketClient(wsConnectSuccess,wsConnectError,wsMessageHandler,wsConnectClose)

  def init()={
    drawBpView.drawGameWait
  }

  def joinGame(playerId: String,
               playerName:String,
               roomId: Long
              ): Unit = {
    val url = GameRoute.getWebSocketUri(dom.document,playerId,playerName,roomId)
    //开启websocket
    webSocketClient.setUp(url)
    //gameloop + gamerender
//    start()
    //用户行为：使用键盘or鼠标(观战模式不响应键盘鼠标事件）
//    if(userType != -1){
//      addActionListenEvent
//    }
  }

  private def wsConnectSuccess(e:Event) = {
    println(s"连接服务器成功")
    e
  }

  private def wsConnectError(e:ErrorEvent) = {
    val playground = dom.document.getElementById("playground")
    println("----wsConnectError")
    e
  }
  private def wsMessageHandler(data:GameMessage):Unit = {

  }

  private def wsConnectClose(e:Event) = {
    println("last Ws close")
    e
  }
}

