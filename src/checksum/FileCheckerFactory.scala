/**
 * File: FileCheckerFactory.scala
 * Package: checksum
 * Autore: Francesco Burato
 * Creazione: 31/mag/2013
 */
package checksum

/**
 * Factory object per le istanze di builders
 * @author Francesco Burato
 *
 */
object FileCheckerFactory {
  def getInstance(s: String): Option[FileCheckBuilder] = s.trim().toLowerCase() match {
    case "md5" => Some(Md5Builder)
    case "update" => Some(LastUpdateBuilder)
    case _ => None
  }
}