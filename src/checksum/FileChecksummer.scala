/**
 * File: FileChecksummer.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum
import java.io.File
/**
 * Trait per realizzare un checksummer generico di files.
 * @author Francesco Burato
 *
 */
trait FileChecksummer {
  /**
   * Calcola il checksum del file passato come argomento
   * @param f Il file di cui calcolare il checksum 
   * @returns Il checksum del file in format di stringa
   */
  def sum(f: File) : String 
}