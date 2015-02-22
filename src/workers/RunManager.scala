/**
 * File: RunManager.scala
 * Package: workers
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package workers

import java.io.File
/**
 * Interfaccia per l'interazione tra threads e hypervisor
 * @author Francesco Burato
 *
 */
trait RunManager {
  /**
   * Comunicazione della conclusione del processing da parte di un worker
   * @param f   File elaborato
   * @param res Risultato dell'elaborazione
   */
  def signalEnd(f: File, res : String)
}