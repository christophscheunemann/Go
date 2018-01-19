package sys

import swing.Panel
import swing.MainFrame
import java.awt.Font
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Dimension
import rescala._


class Window(var score: String, var fields: Array[Array[Field]], var stone: Field, var id: Int) {
  val panel = new Panel {
  preferredSize = new Dimension(windowHeight, windowLength)


    override def paintComponent(g: Graphics2D) {
      super.paintComponent(g)

      g. drawString(score, windowLength / 2, 25)
      g.setColor(Color.BLACK)

      for(x <- 0 to gridCount-1 ) {
        for (y <- 0 to gridCount-1) {
           g.drawRect(fields(x)(y).x, fields(x)(y).y, windowLength / gridCount, (windowHeight - offSet) / gridCount)
        }
      }

      //Colorcodes as explained in Fields and Stone,
      //GRAY == 0
      //BLACK == 1
      //WHITE == 2

      if (stone != null && stone.c == 1) {
       g.setColor(Color.BLACK)
       //Circle just a little bit smaller than the Field
       g.fillOval(stone.x + 2, stone.y + 2 , ((windowLength / gridCount) - 4), ((windowHeight - offSet) / gridCount) - 4)
       println(stone.toString)
     }
     
     if (stone != null && stone.c == 2) {
         g.setColor(Color.RED)
         //Circle just a little bit smaller than the Field
         g.fillOval(stone.x + 2, stone.y + 2 , ((windowLength / gridCount) - 4), ((windowHeight - offSet) / gridCount) - 4)
         println(stone.toString)
      }
   }
 }

  val frame = new MainFrame {
    title = "Go"
    resizable = false
    contents = panel
  }


}
