package sys

import rescala._

import scala.swing.Swing
import scala.swing.Reactions.Reaction
import scala.swing.event.MouseMoved
import scala.swing.event.MouseClicked
import scala.swing.event.MouseReleased
import java.awt.Color

object UI {
  trait FrontEnd extends FrontEndHolder {
    def createFrontEnd(score: Signal[String], fields: Signal[Array[Array[Field]]], stone: Signal[Field], id: Signal[Int]) = {
    UI.id = id
    new UI(score, fields, stone, id)
  }
    lazy val mousePosition = UI.mousePosition
    lazy val mouseClicked = UI.mouseClicked
    lazy val id = UI.id
  }

  //for mousepostion of players
  private val mousePositionChanged = Evt[Point]

  val currentMousePosition = mousePositionChanged latest Point(0,0)
  val mousePosition = tick snapshot currentMousePosition


  var mouseClicked = Signal { false }

  var id =  Signal { 0 }

    val reaction: Reaction = {
      case e: MouseMoved =>
        mousePositionChanged(Point(e.point.x, e.point.y))
      case e: MouseClicked =>
      println("clicked Mouse")
      mouseClicked = Signal { true }
      case e: MouseReleased =>
      println("released Mouse")
      mouseClicked = Signal { false }
    }
}

class UI(score: Signal[String], fields: Signal[Array[Array[Field]]], stone: Signal[Field], id: Signal[Int]) extends FrontEnd {
  lazy val window = {
    val window = new Window((score withDefault "").now, fields = null, stone = null, id = 0)

        window.panel.listenTo(window.panel.mouse.moves, window.panel.mouse.clicks)
        window.panel.reactions += UI.reaction

        tick += {
          _ => window.frame.repaint
        }
      window
    }

    score.changed += {
      score => Swing onEDT {
        window.score = score
      }
    }

    fields.changed += {
      fields => Swing onEDT {
        window.fields = fields
      }
    }

    stone.changed += {
      stone => Swing onEDT {
        window.stone = stone
        window
      }
    }

  Swing onEDT {
    window.frame.visible = true
    tickStart
  }
}
