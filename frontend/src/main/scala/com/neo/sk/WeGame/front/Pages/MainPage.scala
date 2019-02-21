package com.neo.sk.WeGame.front.Pages

import com.neo.sk.WeGame.front.common.PageSwitcher
import mhtml.{Cancelable, Rx, mount}
import org.scalajs.dom

import scala.xml.Elem

object MainPage extends PageSwitcher{


  private val currentPage: Rx[Elem] = currentHashVar.map{
    case "HiBrick" :: Nil => new IndexPage().render
    case "playGame" :: playerId :: playerName :: roomId :: Nil => new GamePage(playerId, playerName, roomId.toLong).render
    case x =>
      println(s"unknown hash: $x")
      <div>Error Page</div>
  }

  def show():Cancelable = {
    println("show successful")
    switchPageByHash()
    val page =
      <div>
        {currentPage}
      </div>
    mount(dom.document.body,page)
  }
}
