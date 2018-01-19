package sys

import rescala._

//Color does not marshal, so if c = 0 means GRAY (UNDEFINED), c = 1 means BLACK, c = 2 means WHITE

case class Field(x: Int, y: Int, length: Int, height: Int, occupied: Boolean, c: Int) {
  def contains(p: Point) = p.x >= x && p.x <= x + length && p.y >= y && p.y <= y + height
}
