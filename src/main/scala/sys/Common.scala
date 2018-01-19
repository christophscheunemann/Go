import rescala._

package object sys {
  val windowHeight = 1000
  val windowLength = 1000
  val gridCount = 10
  val offSet = 50
  val length = windowLength / gridCount
  val height = ((windowHeight - offSet) / gridCount)
  val initField = Field(0, 0, 0, 0, false, 0)
  val initPos = Point(0,0)
  val initStone = Stone(Signal { 0 }, Signal { 0 })

  private lazy val event = Evt[Unit]

  private lazy val thread = {
    val thread = new Thread {
      override def run = while (true) {
        event(())
        Thread sleep 20
      }
    }

    thread setDaemon true
    thread.start
    thread
  }

  val tick: Event[Unit] = event

  def tickStart: Unit = thread
}
