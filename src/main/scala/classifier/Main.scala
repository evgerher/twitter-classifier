package classifier

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.IntegerType
import preprocessing.PreprocessDataset

object Main {
  val DATASET = "twits"

  def main(args: Array[String]) {
    val config = new SparkConf()
      .setMaster("local[*]")
      .setAppName("Test app")
      .set("spark.driver.bindAddress", "127.0.0.1") // todo: remove it later

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    val session = SparkSession.builder()
      .config(config)
      .appName("test")
      .master("local")
      .getOrCreate()

    val trainingDSPath = ModelLoader.getResourcePath(s"${DATASET}/train.csv")
    val data = PreprocessDataset.preprocess(session, trainingDSPath)

//    val training = session.read  // TODO: USE IT IF YOU WISH NOT TO PREPROCESS DATA AND COMMENT A LINE ABOVE
//      .format("csv")
//      .option("header", "true")
//      .load(ModelLoader.getResourcePath(ModelLoader.getResourcePath(s"${dataset}/train.csv"))
//      .load("file:///C:/Users/the_art_of_war/IdeaProjects/twitter-classifier/src/main/resources//train.csv")

    val df = data.withColumn("Sentiment", data.col("Sentiment").cast(IntegerType))
    //  val df = training.withColumn("Sentiment", training.col("Sentiment").cast(DoubleType))
    //df.show(20)


    val Array(train, test) = df.randomSplit(Array[Double](0.7, 0.3), seed = 28)

    val all = df.toDF("ItemID","label","SentimentText")

//    all.show(20)

    val train_df = train.toDF("ItemID", "label", "SentimentText")
    val test_df = test.toDF("ItemID", "label", "SentimentText")

    /* find best model using CV
    val findModel = new MLFindModel()
    findModel.findBestParamsOfBayes(train_df,test_df)
    */

    // train and answer query
    val model = new Model(DATASET)
    model.train(train_df)
    val res = model.get(test_df)
    res.show(20)

    /*
    not forget change local path to  path on your machine and cluster
    just answer query:
    val model = new Model()
    val res = model.get(test_df)
    res.show(20)
     */

    session.stop()
  }
}
