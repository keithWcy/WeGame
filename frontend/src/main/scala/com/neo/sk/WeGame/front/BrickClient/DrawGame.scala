package com.neo.sk.WeGame.front.BrickClient

import com.neo.sk.WeGame.snake.Point
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html.Canvas

case class DrawGame(ctx:CanvasRenderingContext2D,
                    canvas:Canvas,
                    size:Point
                   ) {

  def drawGameWait()={
    ctx.fillStyle = Color.White.toString()
    ctx.fillRect(0, 0, size.x , size.y)
    ctx.fillStyle = "rgba(99, 99, 99, 1)"
    ctx.font = "36px Helvetica"
    ctx.fillText("正在等待游戏连接", 150, 180)
  }

}
