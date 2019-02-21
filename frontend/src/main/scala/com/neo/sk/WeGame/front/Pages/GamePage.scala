package com.neo.sk.WeGame.front.Pages

import com.neo.sk.WeGame.front.BrickClient.GameHolder
import com.neo.sk.WeGame.front.common.Page
import com.neo.sk.WeGame.front.utils.ShortCut

import scala.xml.Elem


class GamePage(playerId:String,playerName:String,roomId:Long) extends Page {
  private val gameView = <canvas id ="GameView" tabindex="1"></canvas>
  private val backgroundView = <canvas id="backgroundView" tabindex="2" ></canvas>


  def init()={
    val gameHolder = new GameHolder
    gameHolder.init()
    //gameHolder.joinGame(playerId,playerName,roomId)
  }
  override def render: Elem = {
      ShortCut.scheduleOnce(() =>init(),0)
    <div>
      {gameView}
      {backgroundView}
    </div>
  }
}
