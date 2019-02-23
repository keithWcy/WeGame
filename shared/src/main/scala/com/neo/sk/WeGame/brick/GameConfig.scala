package com.neo.sk.WeGame.brick

object GameConfig {

  val frameRate = 150  //ms

  val advanceFrame = 0 //客户端提前的帧数

  val delayFrame = 1 //延时帧数，抵消网络延时

  val maxDelayFrame = 3


  case class Point(x: Int, y: Int) {
    def +(other: Point) = Point(x + other.x, y + other.y)

    def -(other: Point) = Point(x - other.x, y - other.y)

    def *(n: Int) = Point(x * n, y * n)

    def %(other: Point) = Point(x % other.x, y % other.y)
  }

  case class player(
                     id:String,
                     name:String,
                     position:Int,//0在上，1在下
                     x:Int,//发射杆位置
                     y:Int,
                     bricks:List[brick],
                     balls:List[ball],
                     amount:Int=3 //弹球数量
                   )

  case class brick(
                    id:String,
                    x:Int,//砖块位置
                    y:Int,
                    length:Int=35,
                    count:Int=3//砖块计数
                  )
  case class ball(
                   id:String,
                   x:Int,//弹球位置
                   y:Int,
                   targetX:Int,//弹球运动方向
                   targety:Int,
                   radius:Int=20,
                   IsDraw:Boolean = true
                 )

}
