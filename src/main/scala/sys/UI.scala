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
    def createFrontEnd(score: Signal[String], stone: Signal[Stone]) = new UI(score, stone)
    lazy val mousePosition = UI.mousePosition
    lazy val mouseClicked = UI.mouseClicked
  }

  //for mousepostion of players
  private val mousePositionChanged = Evt[Point]

  val currentMousePosition = mousePositionChanged latest Point(0,0)
  val mousePosition = tick snapshot currentMousePosition

  //for clicks of players
  private val mouseClickedChanged = Evt[Boolean]

  val currentMouseClicked = mouseClickedChanged latest false
  var mouseClicked = tick snapshot currentMouseClicked

    val reaction: Reaction = {
      case e: MouseMoved =>
        mousePositionChanged(Point(e.point.x, e.point.y))
      case e: MouseClicked =>
      mouseClickedChanged(true)
      Thread.sleep(50)
      mouseClickedChanged(false)
    }
}

class UI(score: Signal[String], stone: Signal[Stone]) extends FrontEnd {
  lazy val window = {
    val window = new Window((score withDefault "").now)

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

    stone.changed += {
      stone => Swing onEDT {
        if (stone != null) {
        window.stone(stone.x)(stone.y) = stone
      }
    }
  }

  Swing onEDT {
    window.frame.visible = true
    tickStart
  }
}
