package recommender.preprocess


import org.apache.beam.runners.dataflow.DataflowRunner
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import com.spotify.scio._
import org.apache.beam.runners.direct.DirectTestOptions
import org.apache.beam.sdk.transforms.Combine.CombineFn
//import org.apache.beam.sdk.options.PipelineOptions.DirectRunner
import source.beam.FileIO


case class Transaction(cardId: String, purchaseAmount: Float) {

  def +(other: Transaction): Transaction =
    Transaction(other.cardId, other.purchaseAmount + purchaseAmount)

}

object Transaction {
  def apply(row: Map[String, Any]): Transaction =
    Transaction(row.get("card_id").toString, row.get("purchase_amount").toString.toFloat)


  def apply(): Transaction = Transaction(null, 0.0f)
}

class DataPreProcessHandler(@transient val sc: ScioContext,
                            val input: String,
                            val output: String) extends FileIO {
  def createFilePipelines(basePath: String): Unit = {
    val totalTransactions = readCsv(sc, basePath + "sample_raw/historical_transactions.csv")
      .union(readCsv(sc, basePath + "sample_raw/new_merchant_transactions.csv"))

    totalTransactions
      .map(Transaction(_))
      .keyBy(_.cardId) // [K,Map[String,Any]]]
      .aggregateByKey(Transaction())(_ + _, _ + _)

    //    totalTransactions.apply

    //    totalTransactions.take(100).map(println)
    //    sc.close().waitUntilFinish()

    //    val train = readCsv(sc, basePath + "train.csv")
    //    val transactions = readCsv(sc, basePath + "historical_transactions.csv")
    //    val merchants = readCsv(sc, basePath + "merchants.csv")
    //    val merchants_transaction = readCsv(sc, basePath + "new_merchant_transactions.csv")

    //    sc.close() // Show results for testing purposes
  }
}

object DataPreProcessHandle {
  def main(cmdlineArgs: Array[String]): Unit = {
    //    val basePath = "gs://bh-kaggle-recommender/"
    val basePath = "./data/"

    // Couldn't find a method for `setStagingLocation`...
    val (sc: ScioContext, args: Args) = ContextAndArgs(
      cmdlineArgs ++ Array(
        "--stagingLocation=" + basePath + "staging",
        "--region=europe-west1"))

    //    sc.options.setRunner(classOf[DataflowRunner])
    //    sc.optionsAs[DataflowPipelineOptions].setProject("bh-gcp-test")
    //    sc.optionsAs[DirectTestOptions]

    sc.options.setJobName("df-scio-test")
    sc.options.setTempLocation(basePath + "temp")

    val input = args.getOrElse("input", basePath + "sample_raw/train.csv")
    val output = args.getOrElse("output", basePath + "processed/")
    val dataProcessor = new DataPreProcessHandler(sc, input, output)

    dataProcessor.createFilePipelines(basePath)

  }
}
