/**
 * File: ThreadBasedManager.scala
 * Package: checkmanager
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checkmanager

import scala.collection.mutable.{Queue, MutableList}
import scala.actors.Actor
import java.io.File
import checksum.FileCheckBuilder
import workers.{ProcessFiles, ThreadWorker, Completion, RunManager}
/**
 * @author Francesco Burato
 *
 */
class ThreadBasedManager(private val builder :FileCheckBuilder, progress : Actor) extends CheckManager(progress) with RunManager {
  private var runningWorkers = 0
  private val finishedWork : Queue[(String,String)] = new Queue[(String,String)]
  def dispatch(q: Queue[File]): MutableList[(String,String)] = { 
    progress ! ProcessFiles(q.length)
    val workers = new Array[ThreadWorker](Configurator.workerPoolSize)
    for( i <- 0 to workers.length-1) {
      // creo il nuovo worker e lo avvio
      workers(i) = new ThreadWorker(this,builder.getInstance())
      new Thread(workers(i)).start
    }
    while(q.nonEmpty) {
      var i = 0
      while(i < workers.length && q.nonEmpty) {
        val f = q.dequeue
        workers(i).setFile(f)
        i += 1
        synchronized {
          runningWorkers += 1
        }
      }
      synchronized{
        while(runningWorkers > 0) 
          wait
      }
    }
    for( i <- 0 to workers.length-1) {
      // creo il nuovo worker e lo avvio
      workers(i).terminate
    }
    val result = finishedWork sortBy (_._1)
    finishedWork.clear
    result
  }
  def signalEnd(f: File, res: String): Unit = synchronized{
    runningWorkers -= 1
    finishedWork += ((f.getAbsolutePath(),res))
    progress ! Completion(finishedWork.length)
    notify
  }

}