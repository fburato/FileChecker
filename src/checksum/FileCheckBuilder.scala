/**
 * File: FileCheckBuilder.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum

/**
 * Trait che gestisce l'allocazione dei checksummer in maniera trasparente
 * @author Francesco Burato
 *
 */
trait FileCheckBuilder {
  /**
   * Ottiene una istanza utilizzabile del FileChecksummer
   */
  def getInstance() : FileChecksummer
}