package sys

import rescala._

import scala.swing.Swing
import scala.swing.Reactions.Reaction
import scala.swing.event.MouseMoved
import scala.swing.event.MouseClicked

import java.awt.Color

object UI {
  trait FrontEnd extends FrontEndHolder {
    def createFrontEnd(score: Signal[String], fields: Signal[Array[Array[Field]]], stone: Signal[Field]) = new UI(score, fields, stone)
    lazy val mousePosition = UI.mousePosition
    lazy val mouseClicked = UI.mouseClicked
  }

  //for mousepostion of players
  private val mousePositionChanged = Evt[Point]

  val currentMousePosition = mousePositionChanged latest Point(0,0)
  val mousePosition = tick snapshot currentMousePosition


  //For Mouseclicks of players
  val mouseClicked = tick snapshot currentMousePosition

  //Reactions of clicks and moves of the mouse of a player
    val reaction: Reaction = {
      case e: MouseMoved =>
        mousePositionChanged(Point(e.point.x, e.point.y))
      case e: MouseClicked =>
      println("clicked")
      println(e.point.x)
      println(e.point.y)
    }
}

class UI(score: Signal[String], fields: Signal[Array[Array[Field]]], stone: Signal[Field]) extends FrontEnd {
  lazy val window = {
    val window = new Window((score withDefault "").now, fields = null, stone = null)

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
      }
    }

  Swing onEDT {
    window.frame.visible = true
    tickStart
  }
}
