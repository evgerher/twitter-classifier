package streamer

import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


import classifier._
import org.apache.spark.rdd.RDD
import preprocessing._

object RSSDemo {
  val DATASET = "twits"
  val queryfeed = "https://queryfeed.net/twitter?title-type=tweet-text-full&order-by=recent&q=%23"
  var tag: String = null
  val durationSeconds = 10
  val sparkSession: SparkSession = initSpark()
  val ssc: StreamingContext = initStreamingContext(sparkSession)
  val tweetPreprocessor: PreprocessTweet = new PreprocessTweet(sparkSession)
  val model: Model = new Model(DATASET)
  var count: Integer = 0

  def initSpark(): SparkSession = {
    val conf = new SparkConf()
      .setAppName("RSS Spark Application")
      .setIfMissing("spark.master", "local[*]")
    //      .set("spark.driver.bindAddress", "127.0.0.1")

    val sc = new SparkContext(conf)
    val sparkSession = SparkSession.builder.config(conf).getOrCreate()

    sc.setLogLevel("ERROR")
    (sparkSession)
  }

  def initStreamingContext(ss: SparkSession): StreamingContext = {
    (new StreamingContext(ss.sparkContext, Seconds(durationSeconds)))
  }

  def main(args: Array[String]) {

    tag = args(0)
    val urlCSV = queryfeed + tag
    val urls = urlCSV.split(",")
    val stream = new RSSInputDStream(urls, Map[String, String](
      "User-Agent" -> "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
    ), ssc, StorageLevel.MEMORY_ONLY, pollingPeriodInSeconds = durationSeconds)
    stream.foreachRDD(rdd=>{
      import sparkSession.sqlContext.implicits._

      val filtered: Dataset[String] = rdd
        .toDS()
        .select("title")
        .map(tweetPreprocessor.preprocessText)
        .filter(_.length > 0)

      val predictedDF = model.get(filtered.toDF("SentimentText"))

      store(rdd, predictedDF.rdd)
    })

    // run forever
    ssc.start()
    ssc.awaitTermination()
  }

  def store(rdd: RDD[RSSEntry], predictions: RDD[Row]) = {
    import sparkSession.sqlContext.implicits._

    rdd
      .saveAsTextFile(s"file:///C:/cygwin64/home/evger/twitter-classifier/temp/${tag}_input${count}")

    predictions
      .saveAsTextFile(s"file:///C:/cygwin64/home/evger/twitter-classifier/temp/${tag}_result${count}")
    //      predictedDF.
    //        .write
    //        .csv(s"file:///C:/cygwin64/home/evger/twitter-classifier/result${count}")
    count += 1

    if (count == 4) {
      ssc.stop(true, true)
      sparkSession.stop()
    }
  }
}
