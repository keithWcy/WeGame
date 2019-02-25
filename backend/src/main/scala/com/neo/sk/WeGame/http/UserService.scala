package com.neo.sk.WeGame.http

import java.util.concurrent.atomic.AtomicInteger

import akka.http.scaladsl.server.Route
import io.circe.generic.auto._

import scala.concurrent.Future
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory
import akka.http.scaladsl.server.Directives._
import com.neo.sk.WeGame.brick.userProtocol
import com.neo.sk.WeGame.brick.userProtocol.RegisterRsp
import com.neo.sk.WeGame.models.{UsersRepo, Users}
import com.neo.sk.WeGame.Boot.{executor, scheduler, timeout, userManager}
import io.circe.Error

trait UserService extends ServiceUtils {

  import io.circe._
  import io.circe.generic.auto._

  private val log = LoggerFactory.getLogger(this.getClass)

  private[this] val idGenerator = new AtomicInteger(1000000)

  private def registerErrorRsp(identity:String,nickname:String) = RegisterRsp(identity,nickname,100001)

  private val signUp=(path("signUp") & post & pathEndOrSingleSlash){
    entity(as[Either[Error,userProtocol.RegisterReq]]){
      case Right(req) =>
        dealFutureResult{
          UsersRepo.checkIdentity(req.nickName).map{r=>
            if(r.isDefined){
              //该邮箱/手机号已经被注册，提示用户重新输入
              complete(registerErrorRsp(req.nickName,"该名字已被注册，请重新输入！"))
            }else{
              if(req.passWord!=req.repassWord){
                complete(registerErrorRsp(req.nickName,"密码不一致"))
              }else{
                val creatTime=System.currentTimeMillis()
                dealFutureResult{
                  UsersRepo.userSignUp(Users(-1,req.nickName,req.nickName,creatTime,0,false)).map{r=>
                    complete(RegisterRsp(req.nickName,req.passWord,0))
                  }
                }
              }
            }
          }.recover{
            case e:Exception =>
              complete(registerErrorRsp(req.nickName,"查找用户信息失败"))
          }
        }
      case Left(error) =>
        log.debug(s"用户注册失败：${error}")
        complete(registerErrorRsp("",s"注册失败：${error}"))
    }
  }

  val UserRoute:Route= pathPrefix("user"){
    signUp
  }

}
