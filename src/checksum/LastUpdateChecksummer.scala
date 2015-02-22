/**
 * File: LastUpdateChecksummer.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum

import java.io.File
import java.text.SimpleDateFormat
/**
 * Classe che realizza il checksum come data di ultima modifica del file
 * @author Francesco Burato
 *
 */
class LastUpdateChecksummer extends FileChecksummer{
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  def sum(f : File) : String = sdf.format(f.lastModified())
}