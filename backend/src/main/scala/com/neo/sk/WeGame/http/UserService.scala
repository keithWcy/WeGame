package com.neo.sk.WeGame.http

import akka.http.scaladsl.server.Route
import io.circe.generic.auto._

import scala.concurrent.Future
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory
import akka.http.scaladsl.server.Directives._

trait UserService extends ServiceUtils {

  private val log = LoggerFactory.getLogger(this.getClass)

//  private val signUp=(path("signUp") & post & pathEndOrSingleSlash){
//
//  }

//  val UserRoute:Route= pathPrefix("user"){
//    signUp
//  }

}
