package sys

import retier._
import retier.architectures.MultiClientServer._
import retier.rescalaTransmitter._
import retier.serializable.upickle._
import retier.tcp._

import java.awt.Color

import rescala._

@multitier
object Go {
  trait Server extends ServerPeer[Client]
  trait Client extends ClientPeer[Server] with FrontEndHolder

  var playerFields = Array.ofDim[Field](gridCount,gridCount)
  var currentPlayer = 0

  val clientMouseX = placed[Client] {
    implicit! =>
    Signal {
      peer.mousePosition().x
    }
  }

  val clientMouseY = placed[Client] {
    implicit! =>
    Signal {
      peer.mousePosition().y
    }
  }

  val clientClick = placed[Client] {
    implicit! =>
    Signal {
      peer.mouseClicked()
    }
  }

  val players = placed[Server].local { implicit! =>
    Signal {
      remote[Client].connected() match {
        case black :: white :: _ => Seq(Some(black), Some(white))
        case _ => Seq(None, None)
      }
    }
  }

  val isPlaying = placed[Server].local {
    implicit! =>
    Signal {
      remote[Client].connected().size >= 2
    }
  }

  val fields = placed[Server] {
    implicit! =>
    val playerFields = Array.ofDim[Field](gridCount,gridCount)
    for(x <- 0 to gridCount-1 ) {
      for (y <- 0 to gridCount-1) {
        if (y == 0) {
          playerFields(x)(y) = new Field(x * (windowLength / gridCount), offSet, windowLength / gridCount, ((windowHeight - offSet) / gridCount), false, 0)
        } else {
          playerFields(x)(y) = new Field(x * (windowLength / gridCount), ((y * ((windowHeight - offSet) / gridCount)) + offSet), windowLength / gridCount, ((windowHeight - offSet) / gridCount), false, 0)
        }
      }
    }
    this.playerFields = playerFields

    Signal {
      playerFields
    }

  }

  val stone: Signal[Field] on Server = placed {
    implicit! =>
    tick.fold(initField) { (stone, _) =>
      if (isPlaying.now) {
        val x = Signal {
          players() map { _ map {
            client => (clientMouseX from client).asLocal()
          } getOrElse 0 }
        }

        val y = Signal {
          players() map { _ map {
            client => (clientMouseY from client).asLocal()
          } getOrElse 0 }
        }

        val clicked = Signal {
          players() map { _ map {
            client => (clientClick from client).asLocal()
          } getOrElse false }
        }

        println("playing: " + Signal { isPlaying() })
        println("print player pos x: " + Signal {x()(currentPlayer)}.now)
        println("print player pos y: " + Signal {y()(currentPlayer)}.now)

        //Initial If to see if the y signal is less than the Grid start at offSet (50 atm), then null is returned
        if ((Signal {y()(0)}.now < 50)|| (Signal{y()(1)}.now < 50 )) {

          //is catched in Window.scala => stone must not be null
          initField

        } else {
          if (Signal { clicked()(currentPlayer)}.now) {
            println("clicked: " + currentPlayer)
            var currentStone = initStone
            val newStone = new Stone(Signal { x()(currentPlayer) }, Signal{ y()(currentPlayer)})
            for(x <- 0 to gridCount-1 ) {
              for (y <- 0 to gridCount-1) {
                if (this.playerFields(x)(y).contains(Point(newStone.x.now, newStone.y.now))) {
                  //recommended in scala to create a new object each time
                  currentStone = new Stone(Signal { this.playerFields(x)(y).x}, Signal { this.playerFields(x)(y).y})
                  this.playerFields(x)(y) = new Field(this.playerFields(x)(y).x, this.playerFields(x)(y).y, length, height, true, currentPlayer + 1)
                }
              }
            }
            //set nextPlayer
            currentPlayer = currentPlayer + 1 % 2

            Field(currentStone.x.now, currentStone.y.now, length, height, true, currentPlayer + 1)

          } else {

            //is catched in Window.scala => stone must not be null
            initField

          }
        }
      } else {
        stone
      }
    }
  }

val blackPlayerPoints = placed[Server].local {
  implicit! =>
  var points = 0
  for(x <- 0 to gridCount-1 ) {
    for (y <- 0 to gridCount-1) {
      if ((this.playerFields(x)(y).occupied) && this.playerFields(x)(y).c == 1) {
        points = points + 1
      }
    }
  }
  Signal {
    points
  }
}

val whitePlayerPoints = placed[Server].local {
  implicit! =>
  var points = 0
  for(x <- 0 to gridCount-1 ) {
    for (y <- 0 to gridCount-1) {
      if ((this.playerFields(x)(y).occupied) && this.playerFields(x)(y).c == 2) {
        points = points + 1
      }
    }
  }
  Signal {
    points
  }
}

val score = placed[Server] {
  implicit! =>

  Signal {
    blackPlayerPoints() + " : " + whitePlayerPoints()
  }
}

val id = placed[Server] {
  implicit! =>
  Signal {
    remote[Client].connected().size
  }
}

val frontEnd = placed[Client].local {
  implicit! =>
  peer.createFrontEnd(score.asLocal, fields.asLocal, stone.asLocal, id.asLocal)
}

tickStart

}

object GoServer extends App {
  retier.multitier setup new Go.Server{
    def connect = TCP(1099)
  }
}

object GoClient extends App {
  retier.multitier setup new Go.Client with UI.FrontEnd {
    def connect = TCP("localhost", 1099)
  }
}
