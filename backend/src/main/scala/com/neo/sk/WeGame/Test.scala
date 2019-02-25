package com.neo.sk.WeGame

import akka.http.scaladsl.Http
import com.neo.sk.WeGame.Boot.{log, routes}
import com.neo.sk.WeGame.common.AppSettings.{httpInterface, httpPort}
import com.neo.sk.WeGame.models.UsersRepo.UsersTableQuery
import com.neo.sk.WeGame.models.Users
import com.neo.sk.WeGame.utils.DBUtil.db
import com.neo.sk.WeGame.models.UsersRepo._

object Test {

  def main(args: Array[String]): Unit = {
    userSignUp(Users(-1,"123","123",0,0,false))
  }

}
