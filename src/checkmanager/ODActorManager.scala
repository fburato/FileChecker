/**
 * File: ODActorManager.scala
 * Package: checkmanager
 * Autore: Francesco Burato
 * Creazione: 10/lug/2013
 */
package checkmanager

import scala.collection.mutable.{Queue, MutableList}
import scala.actors.Actor

import java.io.File
import checksum.FileCheckBuilder
import workers.{Completion, ActorWorker, ProcessFiles, Response, CheckSum, Terminate}
/**
 * @author Francesco Burato
 *
 */
class ODActorManager(private val builder: FileCheckBuilder, progress : Actor) extends CheckManager(progress) {
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
      // invio i primi file da elaborare 
      var sentFiles = 0
      while(sentFiles < workers.length && queuedFiles.nonEmpty) {
        val f = queuedFiles.dequeue
        workers(sentFiles) ! CheckSum(f)
        sentFiles += 1
      }
      // per il resto dell'elaborazione rimango in attesa di risposta dai workers
      // e appena ricevo risposte invio altro lavoro al mittente
      var i = 0
      while(sentFiles > 0) {
        receive {
          case Response(f, res) =>
            finishedWork += ((f.getCanonicalPath(),res))
            i += 1
            progress ! Completion(i)
            if(queuedFiles.nonEmpty){
              val f = queuedFiles.dequeue
              sender ! CheckSum(f)
            } else 
              sentFiles -= 1
        }
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