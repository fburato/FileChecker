/**
 * File: CheckManager.scala
 * Package: checkmanager
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checkmanager

import java.io.File

import scala.collection.mutable.Queue
import scala.collection.mutable.MutableList
import scala.actors.Actor
import workers.ProcessFiles
/**
 * Manager generale per la gestione delle checksum
 * @author Francesco Burato
 *
 */
abstract class CheckManager(val progress : Actor) {
  /**
   * Effettua l'esplorazione di tutti i file a partire dal punto passato come argomento
   * @param s Starting point della ricerca dei file leggibili
   */
  private val q = new Queue[File]
  /**
   * Esegue l'esplorazione del filesystem a partire dal file fornito
   */
  def explore(f: File): Unit = if (f.canRead())
    //eseguo solo se il file è leggibile
    if (!f.isDirectory())
      // aggiungo il file regolare alla coda
      q += f
    else if (f.isDirectory() && f.canExecute())
      // se è una directory leggibile esploro ricorsivamente tutti i file
      for (i <- f.listFiles())
        explore(i)

  /**
   * Esegue il checksum dei file estrapolati passati come argomento
   * @param q Coda di file da esaminare
   */
  def dispatch(q : Queue[File]): MutableList[(String, String)]
  
  /**
   * Esecutore dell'intero processo di esplorazione
   * @param start Path del file da cui far partire l'elaborazione
   */
  def execute(start: String) : MutableList[(String, String)] = {
    val f = new File(start)
    if(f.exists()) {
      // procedi all'esplorazione solo se il file passato esiste
      explore(f)
      dispatch(q)
    } else {
      // altrimenti restituisce una lista vuota
      progress ! ProcessFiles(0)
      new Queue[(String,String)]
    }
  }
}