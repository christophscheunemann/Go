package sys

import swing.Panel
import swing.MainFrame
import java.awt.Font
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Dimension
import rescala._


class Window(var score: String) {
  val stone = Array.ofDim[Stone](gridCount, gridCount)

  val panel = new Panel {
    preferredSize = new Dimension(windowHeight, windowLength)

    override def paintComponent(g: Graphics2D) {
      super.paintComponent(g)


      g. drawString(score, windowLength / 2, 25)

      //Colorcodes,
      //GRAY == 0
      //BLACK == 1
      //WHITE == 2

      for(x <- 0 to gridCount-1 ) {
        for (y <- 0 to gridCount-1) {
          g.setColor(Color.BLACK)
          g.drawRect(x * length , offSet + (y * height), length, height)
          if (stone(x)(y) != null) {
            val tmp = stone(x)(y)
            if (tmp.c == 1) {
              g.setColor(Color.BLACK)
            } else if (tmp.c == 2){
              g.setColor(Color.RED)
            }
            g.fillOval(tmp.x + 2, tmp.y + 2 , length - 4, height - 4)
          }
        }
      }
    }
  }

  val frame = new MainFrame {
    title = "Go"
    resizable = false
    contents = panel
  }


}
