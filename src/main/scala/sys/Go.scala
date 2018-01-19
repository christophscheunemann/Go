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


  //
  //give feedback on postion change of mouse of a client, or if he clicks
  //
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
      peer.mouseClicked
    }
  }

  val isPlaying = placed[Server].local { implicit! =>
    Signal {
      remote[Client].connected().size >= 2
    }
  }


  //sequence to get specified Mouse information of a player
  val players = placed[Server].local { implicit! =>
    Signal {
      remote[Client].connected() match {
        case black :: white :: _ => Seq(Some(black), Some(white))
        case _ => Seq(None, None)
      }
    }
  }

    //fields of players
  val fields = placed[Server] {
    implicit! =>
    val playerFields = Array.ofDim[Field](gridCount,gridCount)
       for(x <- 0 to gridCount-1 ) {
            for (y <- 0 to gridCount-1) {
              //first run, add the offset for the Score
              if (y == 0) {
                 playerFields(x)(y) = new Field(x * (windowLength / gridCount), offSet, windowLength / gridCount, ((windowHeight - offSet) / gridCount), false, 0)
              } else {
                //All other runs
                playerFields(x)(y) = new Field(x * (windowLength / gridCount), ((y * ((windowHeight - offSet) / gridCount)) + offSet),
                windowLength / gridCount, ((windowHeight - offSet) / gridCount), false, 0)}
             }
            }
          this.playerFields = playerFields

        Signal {
          playerFields
        }

    }

    /*
    TODO Hauptproblem:
    stone wird im frontEnd erstellt asLocal, der server soll hier die x und y Werte von jedem player in der
    sequence wie in der funktion players speichern (beides sind Signale, deswegen geht das auslesen und ständig abfragen)

    For further info read: http://www.guidosalvaneschi.com/rescala/main/manual.pdf
    */
    val stone = placed[Server] {
      implicit! =>

      //get postion of the mouse and check whether a client has clicked or not
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

      /*
      Ab hier wirds interessant: Die if abfrage funktioniert noch net so wie gewollt: Es soll folgendes tun:
      Ändert sich der x bzw y Wert von einem der Player und klickt derjenige an dieser Position soll ein neuer stein erstellt
      und gesetzt an der entsprechenden Position. Bisher sind beide bedingungen aber immer false, deswegen wird null zurückgeliefert
      was in der Window.scala gecatched wird (das null ist gewollt). Setze eine der Bedinungen auf true dann wird auch an der richtigen stelle ein stein der
      entsprechenden Farbe gesetzt. (siehe auch print outs in der client console das er es immer wieder malt) Es sind also nur die
      Bediungen noch falsch hab aber noch keine Lösung gefunden: Der Klick über eine Boolean muss noch dazu getan werden, passende funktion
      auf dem client der den Click erkennt steht bereit (weiter oben clientClick).
      Restlichen Funktionen funktionieren soweit, Punkte werden durch eine Forschleife durch das Grid Array immer wieder neu gerechnet.
      */

      //Initial If to see if the y signal is less than the Grid start at offSet (50 atm), then null is returned
      // Mach ich jetz fertig wolltet ja erstmal nen commit :P
      /*
      if (Signal { (y()(0) } < 50) || Signal { (y()(1) } < 50 )) {
        println("here")
        Signal {
          //is catched in Window.scala => stone must not be null
          null
        }
      }
      */



       if (Signal {remote[Client].connected() } == Signal {players()(0)}) {
         var blackStone = new Stone(Signal { 0 }, Signal { 0 })
         val newStone = new Stone(Signal { x()(0) }, Signal{ y()(0)})
         for(x <- 0 to gridCount-1 ) {
              for (y <- 0 to gridCount-1) {
                if (this.playerFields(x)(y).contains(Point(newStone.x.now, newStone.y.now))) {
                    blackStone = new Stone(Signal { this.playerFields(x)(y).x}, Signal { this.playerFields(x)(y).y})
                }
              }
            }
         Signal {
           Field(blackStone.x(), blackStone.y() , windowLength / gridCount, ((windowHeight - offSet) / gridCount), true, 1)
         }
       } else if (Signal {remote[Client].connected() } == Signal {players()(1)}) {
         var whiteStone = new Stone(Signal { 0 }, Signal { 0 })
         val newStone = new Stone(Signal { x()(0) }, Signal{ y()(0)})
         for(x <- 0 to gridCount-1 ) {
              for (y <- 0 to gridCount-1) {
                if (this.playerFields(x)(y).contains(Point(newStone.x.now, newStone.y.now))) {
                    whiteStone = new Stone(Signal { this.playerFields(x)(y).x}, Signal { this.playerFields(x)(y).y})
                }
              }
            }
            Signal {
              Field(whiteStone.x(), whiteStone.y(), windowLength / gridCount, ((windowHeight - offSet) / gridCount), true, 2)
            }
          } else {
            Signal{
              //is catched in Window.scala => stone must not be null
            null
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
    //score as Signal String at Server, iterates if a player sets a Stone
  val score = placed[Server] {
    implicit! =>

    Signal {
      blackPlayerPoints() + " : " + whitePlayerPoints()
    }
  }

  val countStones = placed[Server].local {
    implicit! =>
    var blackStones = 0
    var whiteStones = 0


  }

  //
  //init FrontEnd of Client
  //
  val frontEnd = placed[Client].local {
    implicit! =>
    peer.createFrontEnd(score.asLocal, fields.asLocal, stone.asLocal)
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
