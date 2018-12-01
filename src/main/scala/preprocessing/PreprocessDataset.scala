package preprocessing

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.IntegerType

class PreprocessDataset {

  def preprocess() {
    val config = new SparkConf()
      .setMaster("local[*]")
      .setAppName("Test app")
      .set("spark.driver.bindAddress", "127.0.0.1")

    val session = SparkSession.builder()
      .config(config)
      .appName("test")
      .master("local")
      .getOrCreate()

    val training = session.read
      .format("csv")
      .option("header", "true")
      .load("file:///C:/Users/Aline/IdeaProjects/twitter-classifier/src/main/resources//train.csv")
    val df = training.withColumn("Sentiment", training.col("Sentiment").cast(IntegerType))

    val datasetList = df.select("SentimentText").rdd.map(r => r(0)).collect().toList

  }
}
