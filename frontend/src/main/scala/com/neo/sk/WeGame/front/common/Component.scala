package com.neo.sk.WeGame.front.common

import scala.language.implicitConversions
import scala.xml.Elem

trait Component {

  def render: Elem

}

object Component {
  implicit def component2Element(comp: Component): Elem = comp.render
}

