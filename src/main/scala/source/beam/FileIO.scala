package source.beam

import java.nio.channels.Channels

import scala.io.Source.fromInputStream
import source.string.StringTransformer
import com.spotify.scio.ScioContext
import com.spotify.scio.coders.Coder
import com.spotify.scio.values.SCollection
import org.apache.beam.sdk.io.FileSystems
import org.apache.beam.sdk.options.PipelineOptionsFactory

class FileIO extends StringTransformer with Serializable {
  implicit def coderAny: Coder[Any] = Coder.kryo[Any]

  /**
    * Split line into KV structure.
    *
    * @param columnNames : Column names of the CSV that's being read.
    * @param sep         : Separator (default: ,)
    * @param line        : Line of CSV to process.
    * @return: Map of values.
    */
  private def valueSplit(columnNames: Seq[String], sep: String)(line: String): Map[String, Any] = {
    val lineSplit = line.split(sep)

    columnNames.zip(lineSplit.map(_.matchDataType)).toMap
  }

  /**
    * Read _only_ the headers of the input CSV (also from GCS).
    *
    * @param path   : Path to CSV.
    * @param sep    : Separator (default: ,)
    * @param header : Whether to use a header. Otherwise, this will return c_1, c_2, etc.
    * @return: Array of column headers.
    */
  private def readCsvColumnHeaders(path: String,
                                   sep: String = ",",
                                   header: Boolean = true): Array[String] = {
    // Register GS filesystem.
    val options = PipelineOptionsFactory.create()
    FileSystems.setDefaultPipelineOptions(options)

    // Read file as input stream.
    val src = Channels
      .newInputStream(
        FileSystems.open(
          FileSystems
            .matchSingleFileSpec(path)
            .resourceId()))

    // Get first line of file.
    val headerLine = fromInputStream(src).getLines.next.split(sep)

    src.close()

    // Either return c_1, c_2, etc., or an array of column names.
    if (header) headerLine else headerLine.zipWithIndex.map { case (_, i) => s"c_$i" }
  }


  /**
    * Read CSV into SCollection.
    *
    * @param sc     : ScioContext
    * @param path   : Path to CSV.
    * @param sep    : Separator (default: ,)
    * @param header : Whether the CSV has a header.
    * @return: SCollection of KV's.
    */
  def readCsv(sc: ScioContext,
              path: String,
              sep: String = ",",
              header: Boolean = true): SCollection[Map[String, Any]] = {
    val columnNames = readCsvColumnHeaders(path, sep, header)

    sc
      .textFile(path)
      .map(valueSplit(columnNames, sep))
  }

}
