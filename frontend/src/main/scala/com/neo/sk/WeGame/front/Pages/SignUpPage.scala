package com.neo.sk.WeGame.front.Pages

import com.neo.sk.WeGame.brick.userProtocol.{RegisterReq, RegisterRsp}
import com.neo.sk.WeGame.front.common.Page
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.KeyboardEvent
import com.neo.sk.WeGame.front.common.Routes._
import com.neo.sk.WeGame.front.utils.{Http, ShortCut}
import io.circe.generic.auto._
import io.circe._
import io.circe.syntax._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.xml.{Elem, Node}

class SignUpPage extends Page{
  val UserName:Var[Node] =Var(
    <div>
      <label style="text-align:right" >用户名</label>
      <div class="col-md-6">
        <input type="text" id="username" placeholder="用户名"  autofocus="true"></input>
      </div>
    </div>
  )

  val PassWord:Var[Node] =Var(
    <div>
      <label style="text-align:right;">密码</label>
      <div class="col-md-6">
        <input type="password" id="password" placeholder="密码"></input>
      </div>
    </div>
  )


  val rePassWord:Var[Node] =Var(
    <div>
      <label style="text-align:right;">密码确认</label>
      <div class="col-md-6">
        <input type="password" id="repassword" placeholder="重复密码"></input>
      </div>
    </div>
  )


  val Form:Var[Node]=Var(
    <form style="border: 1px solid #dfdbdb;border-radius: 6px;padding:2rem 1rem 2rem 1rem;">
      {UserName}
      {PassWord}
      {rePassWord}
    </form>
  )

  val conFirmButton:Var[Node]=Var(
    <div style="padding: 1rem 1rem 1rem 1rem;text-align:center;">
      <button id="logIn"  style="margin: 0rem 1rem 0rem 1rem;" onclick={()=>signUp()}>
        注册
      </button>
    </div>
  )

  val returenButton:Var[Node]=Var(
    <div style="padding: 1rem 1rem 1rem 1rem;text-align:center;">
      <button id="return"  style="margin: 0rem 1rem 0rem 1rem;" onclick={()=>reTurn() } >
        返回
      </button>
    </div>
  )

  def signUp():Unit={
    val name=dom.window.document.getElementById("username").asInstanceOf[Input].value
    val passWord=dom.window.document.getElementById("password").asInstanceOf[Input].value
    val repassWord=dom.window.document.getElementById("repassword").asInstanceOf[Input].value
    val data=RegisterReq(name,passWord,repassWord).asJson.noSpaces
    val url=signUpUrL
    Http.postJsonAndParse[RegisterRsp](url,data).map{
      rsp=>
        if(rsp.errCode==0){
          ShortCut.redirect(s"brick/welCome")
        }else{
          dom.window.alert(rsp.nickname)
        }
    }
  }

  def reTurn()={
    ShortCut.redirect("brick/welCome")
  }


  override def render: Elem ={
    <div>
      {Form}
      <div>
        {conFirmButton}
        {returenButton}
      </div>
    </div>
  }

}
