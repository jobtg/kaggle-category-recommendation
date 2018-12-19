package source.beam

import source.string.StringTransformer
import com.spotify.scio.ScioContext
import com.spotify.scio.coders.Coder
import com.spotify.scio.values.SCollection

import scala.io.Source

class FileIO extends StringTransformer with Serializable {
  implicit def coderObject: Coder[Object] = Coder.kryo[Object]

  private def keyValueSplit(line: String, columnNames: Seq[String], keyIndex: Int, sep: String): Seq[Object] = {
    val lineSplit = line.split(sep)

    Seq(lineSplit(keyIndex), columnNames.zip(lineSplit.map(_.matchDataType)).toMap)
  }

  private def readCsvColumnHeaders(path: String,
                                   sep: String = ",",
                                   header: Boolean = true): Array[String] = {
    val src = Source.fromFile(path)
    val line = src.getLines.next().split(sep)

    src.close()

    if (header) line else line.zipWithIndex.map { case (_, i) => s"c_$i" }
  }

  def readCsv(sc: ScioContext,
              path: String,
              keyColumn: String = "card_id",
              sep: String = ",",
              header: Boolean = true): SCollection[Seq[Object]] = {
    val columnNames = readCsvColumnHeaders(path, sep, header)
    val keyIndex = columnNames.indexOf(keyColumn)

    sc
      .textFile(path)
      .map(x => keyValueSplit(x, columnNames, keyIndex, sep))
  }

}
