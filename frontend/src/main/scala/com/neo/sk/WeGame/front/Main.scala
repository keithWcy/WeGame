package com.neo.sk.WeGame.front

import com.neo.sk.WeGame.front.Pages.MainPage
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


@JSExportTopLevel("front.Main")
object Main {

  def main(args: Array[String]): Unit ={
    run()
  }

  @JSExport
  def run(): Unit = {
    println("ssssssssss")
    MainPage.show()
  }
}

