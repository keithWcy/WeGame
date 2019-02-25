package com.neo.sk.WeGame.brick

object userProtocol {
  trait CommonRsp {
    val errCode: Int
    val msg: String
  }


  final case class ErrorRsp(
                             errCode: Int,
                             msg: String
                           ) extends CommonRsp

  final case class SuccessRsp(
                               errCode: Int = 0,
                               msg: String = "ok"
                             ) extends CommonRsp

  //登录
  case class LoginReq(
                       idenTity:String,
                       passWord:String
                     )
  case class LoginRsp(
                       identity:String,
                       nickname:String,
                       errCode:Int = 0,
                     )

  //注册
  case class RegisterReq(
                          nickName:String,
                          passWord:String,
                          repassWord:String
                        )

  case class RegisterRsp(
                          identity:String,
                          nickname:String,
                          errCode:Int = 0,
                        )


}
