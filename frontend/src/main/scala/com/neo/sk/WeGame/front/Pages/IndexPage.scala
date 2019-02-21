package com.neo.sk.WeGame.front.Pages

import com.neo.sk.WeGame.front.common.Page
import com.neo.sk.WeGame.front.utils.ShortCut

import scala.xml.Elem

class IndexPage extends Page {

  private val PlayButton = <button id="play" class="button" onclike={()=>anonyPlay()}>游客模式</button>
  private val LoginButton = <button id="login" class="button button2">玩家登录</button>

  def anonyPlay()={
    val PlayerId=System.currentTimeMillis().toString
    val PlayerName="游客"
    val url=s"WeGame/brick/playGame/$PlayerId/$PlayerName/"
    ShortCut.redirect(url) //是否会往这个URL发请求？
  }

  override def render: Elem ={
    println("index")
    <div>
      {PlayButton}
      {LoginButton}
    </div>
  }

}