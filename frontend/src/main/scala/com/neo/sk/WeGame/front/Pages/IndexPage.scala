package com.neo.sk.WeGame.front.Pages

import com.neo.sk.WeGame.front.common.Page
import com.neo.sk.WeGame.front.utils.ShortCut

import scala.xml.Elem

class IndexPage extends Page {

  private val PlayButton = <button id="play" class="button" onclick={()=>anonyPlay()}>游客模式</button>
  private val LoginButton = <button id="login" class="button button2">玩家登录</button>

  def anonyPlay()={
    val playerId=System.currentTimeMillis().toString
    val playerName="游客"
    val url=s"brick/playGame?playerId=$playerId&playerName=$playerName"
    ShortCut.redirect(url) //是否会往这个URL发请求？
  }

  override def render: Elem ={
    <div>
      {PlayButton}
      {LoginButton}
    </div>
  }

}