package com.neo.sk.WeGame.front.Pages

import com.neo.sk.WeGame.front.common.Page
import com.neo.sk.WeGame.front.utils.ShortCut

import scala.xml.Elem

class IndexPage extends Page {

  private val PlayButton = <button id="play" class="button" onclick={()=>anonyPlay()} style="margin:200px 70px 100px 100px">游客模式</button>
  private val LoginButton = <button id="login" class="button button2" style="margin:200px 70px 100px 70px">玩家登录</button>
  private val SignUpButton = <button id="signup" class="button button3" style="margin:200px 70px 100px 70px" onclick={()=>signUp()}>账号注册</button>
  private val adminButton = <button id="admin" class="button button4" style="margin:200px 70px 100px 70px">管理游戏</button>

  def anonyPlay()={
    val playerId=System.currentTimeMillis().toString
    val playerName="游客"
    val url=s"brick/playGame?playerId=$playerId&playerName=$playerName"
    ShortCut.redirect(url) //是否会往这个URL发请求？
  }

  def signUp()={
    val url="brick#/SignUp"
    ShortCut.redirect(url)
  }

  override def render: Elem ={
    <div>
      {PlayButton}
      {LoginButton}
      {SignUpButton}
      {adminButton}
    </div>
  }

}