package sys

import rescala.Signal

import scala.swing.Reactions.Reaction
import java.awt.event.MouseEvent
import java.awt.Color

trait FrontEnd

trait FrontEndHolder {
  def createFrontEnd(score: Signal[String], fields: Signal[Array[Array[Field]]], stone: Signal[Field], id: Signal[Int]): FrontEnd
  val mousePosition: Signal[Point]
  val mouseClicked: Signal[Boolean]
  val id: Signal[Int]
}
