/**
 * File: ActorWorker.scala
 * Package: workers
 * Autore: Francesco Burato
 * Creazione: 01/giu/2013
 */
package workers

import checksum.FileChecksummer
import java.io.File
import scala.actors.Actor
/**
 * Worker per la comunicazione basata sul modello ad attori
 * @author Francesco Burato
 *
 */
class ActorWorker(
    private val fc: FileChecksummer) extends Actor {
  def act() {
    react {
      case CheckSum(f) =>
        val check = fc.sum(f)
        sender ! Response(f, check)
        act()
      case Terminate() =>
    }
  }
}