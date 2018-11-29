package classifier

import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.IntegerType

object Main {
  def main(args: Array[String]) {
    val config = new SparkConf()
      .setMaster("local[*]")
      .setAppName("Test app")
      .set("spark.driver.bindAddress", "127.0.0.1") // todo: remove it later
//    val sc = new SparkContext(config)

    val session = SparkSession.builder()
      .config(config)
      .appName("test")
      .master("local")
      .getOrCreate()

    val training = session.read
      .format("csv")
      .option("header", "true")
      .load("file:///C:/cygwin64/home/evger/twitter-classifier/src/main/resources/train.csv")
    val df = training.withColumn("Sentiment", training.col("Sentiment").cast(IntegerType))
    df.show(20)

    val Array(train, test) = df.randomSplit(Array[Double](0.7, 0.3))

    val dfs = train.toDF("ItemID", "label", "SentimentText")

    val tokenizer = new Tokenizer()
      .setInputCol("SentimentText")
      .setOutputCol("Variants")

    val hashingTF = new HashingTF()
      .setNumFeatures(1000)
      .setInputCol(tokenizer.getOutputCol)
      .setOutputCol("features")

    val lr = new LogisticRegression()
      .setMaxIter(100)
      .setRegParam(0.001)

    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, hashingTF, lr))

//    val lsvc = new LinearSVC()
//      .setMaxIter(10)
//      .setRegParam(0.1)

    val model = pipeline.fit(dfs)
    println("###" * 10)
    println(model)
    println("###" * 10)
    val observations = model.transform(test.toDF("ItemID", "Sentiment", "SentimentText"))

    val predictionLabelsRDD = observations.select("prediction", "Sentiment").rdd.map { r =>
      val a = java.lang.Double.parseDouble(r.get(0).toString)
      val b = java.lang.Double.parseDouble(r.get(1).toString)
      (a * 1.0, b * 1.0)
    }
    val metrics = new BinaryClassificationMetrics(predictionLabelsRDD)
    val precision = metrics.precisionByThreshold
    precision.foreach { case (t, p) =>
      println(s"Threshold: $t, Precision: $p")
    }

    session.stop()
  }
}
