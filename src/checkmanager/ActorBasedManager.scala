/**
 * File: ActorBasedManager.scala
 * Package: checkmanager
 * Autore: Francesco Burato
 * Creazione: 01/giu/2013
 */
package checkmanager

import scala.collection.mutable.{Queue, MutableList}
import scala.actors.Actor

import java.io.File
import checksum.FileCheckBuilder
import workers.{Completion, ActorWorker, ProcessFiles, Response, CheckSum, Terminate}

/**
 * Manager per la comunicazione basata sul modello ad attori.
 * @author Francesco Burato
 */
class ActorBasedManager(private val builder: FileCheckBuilder, progress : Actor) extends CheckManager(progress) {
  private val finishedWork: Queue[(String, String)] = new Queue[(String, String)]
  private var waitConclusion = false
  /**
   * Esecutore reale della getione dei file
   * @author Francesco Burato
   */
  private class Operator(val queuedFiles: Queue[File], val lock : AnyRef) extends Actor {
    def act() {
      progress ! ProcessFiles(queuedFiles.length)
      val workers = new Array[ActorWorker](Configurator.workerPoolSize)
      for (i <- 0 to workers.length - 1) {
        // creo il nuovo worker e lo avvio
        workers(i) = new ActorWorker(builder.getInstance())
        workers(i).start
      }
      // mando tutto il lavoro ai workers
      var i = 0
      val numFiles = queuedFiles.length
      while (queuedFiles.nonEmpty) {
        val f = queuedFiles.dequeue
        workers(i) ! CheckSum(f)
        i = (i + 1) % Configurator.workerPoolSize
      }

      // ricevo tutti i messaggi e li raccolgo 
      i = 0
      while (i < numFiles)
        receive {
          case Response(f, res) =>
            finishedWork += ((f.getCanonicalPath(), res))
            i += 1
            progress ! Completion(i)
        }

      // sospendo tutti gli attori
      for (w <- workers)
        w ! Terminate()
      waitConclusion = true
      // risveglio il metodo di attesa esterno
      lock.synchronized {
        lock.notify
      }
    }
  }

  def dispatch(q: Queue[File]): MutableList[(String, String)] = {
    waitConclusion = false
    (new Operator(q.clone,this)).start()
    synchronized {
      while(!waitConclusion)
        wait
    }
    val result = finishedWork sortBy (_._1)
    finishedWork.clear
    result
  }
}