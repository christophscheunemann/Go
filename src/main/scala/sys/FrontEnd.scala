package sys

import rescala.Signal

import scala.swing.Reactions.Reaction
import java.awt.event.MouseEvent
import java.awt.Color

trait FrontEnd

trait FrontEndHolder {
  def createFrontEnd(score: Signal[String], stone: Signal[Stone]): FrontEnd
  val mousePosition: Signal[Point]
  val mouseClicked: Signal[Boolean]
}
