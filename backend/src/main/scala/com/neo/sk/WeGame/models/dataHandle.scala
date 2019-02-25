package com.neo.sk.WeGame.models

import scala.concurrent.Future

/**
  * create by zhaoyin
  * 2019/2/22  10:24 PM
  */
case class Users(id:Long, username:String, password:String,createtime:Long, score:Int, is_ban:Boolean)

trait UsersTable {

  import com.neo.sk.WeGame.utils.DBUtil.driver.api._

  class UsersTable(tag: Tag) extends Table[Users](tag, "USERS") {
    val id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    val username = column[String]("USERNAME")
    val password = column[String]("PASSWORD")
    val createtime = column[Long]("CREATETIME")
    val score = column[Int]("SCORE")
    val is_ban = column[Boolean]("IS_BAN")

    def * = (id, username, password, createtime, score, is_ban) <> (Users.tupled, Users.unapply)
  }

  protected val UsersTableQuery = TableQuery[UsersTable]
}

object UsersRepo extends UsersTable {

  import com.neo.sk.WeGame.utils.DBUtil.driver.api._
  import com.neo.sk.WeGame.utils.DBUtil.db

  def checkIdentity(identity: String) = {
    db.run(UsersTableQuery.filter(u => u.username === identity).result.headOption)
  }

  def userLogin(username: String,password:String) = {
    db.run(UsersTableQuery.filter(u => u.username===username && u.password===password).result.headOption)
  }

  def userSignUp(userInfo:Users)={
    db.run(UsersTableQuery.insertOrUpdate(userInfo))
  }

}
