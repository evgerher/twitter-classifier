import breeze.linalg.DenseVector
import org.apache.spark.ml.Pipeline
import org.apache.spark._
import org.apache.spark.ml.classification.{LinearSVC, LogisticRegression}
import org.apache.spark.ml.feature.{HashingTF, LabeledPoint, Tokenizer}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.mllib.linalg.VectorUDT
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.rdd.RDD

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
      .setMaxIter(10)
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
      .select("probability")
    session.stop()
  }
}
