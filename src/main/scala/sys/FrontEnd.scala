package sys

import rescala.Signal

import scala.swing.Reactions.Reaction

trait FrontEnd

trait FrontEndHolder {
  def createFrontEnd(score: Signal[String], fields: Signal[Array[Array[Field]]], stone: Signal[Field]): FrontEnd
  val mousePosition: Signal[Point]
  val mouseClicked: Signal[Point]
}
