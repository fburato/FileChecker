/**
 * File: ThreadWorker.scala
 * Package: workers
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package workers

import checksum.FileChecksummer
import java.io.File

/**
 * Worker thread based per il calcolo del checksum di un insieme di file
 * @author Francesco Burato
 *
 */
class ThreadWorker(
  private val man: RunManager,
  private val fc: FileChecksummer) extends Runnable {
  private var workFile: Option[File] = None // il file in elaborazione dal worker
  private var cont = true // flag di prosecuzione 
  
  private def continue() : Boolean = synchronized{ cont }
  private def setContinue(b : Boolean) = synchronized {cont = b}
  def setFile(f: File) = synchronized {
    f match {
      case null => workFile = None
      case x => workFile = Some(x)
    }
    notify
  }
  
  def terminate() = synchronized {
    cont = false 
    notify
  }
  def run() {
    // funzione sincronizzata per l'attesa di nuovo lavoro
    val waitWork = () => synchronized {
      while(continue && workFile == None)
        wait
    }
    // funzione sincronizzata per il controllo della prosecuzione del ciclo
    val stayCond =() => synchronized {
      continue && workFile != None
    }
    waitWork()
    while(stayCond()) {
      val f = workFile.get
      workFile = None
      man.signalEnd(f, fc.sum(f))
      waitWork()
    }
  }
}