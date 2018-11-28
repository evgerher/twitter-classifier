import org.apache.spark.ml.Pipeline
import org.apache.spark._
import org.apache.spark.ml.classification.{LinearSVC, LogisticRegression}
import org.apache.spark.ml.feature.{HashingTF, LabeledPoint, Tokenizer}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.sql.{SparkSession}
import org.apache.spark.sql.types.IntegerType

import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}

import org.apache.log4j.{Level, Logger}
object Main {
  def main(args: Array[String]) {
    val config = new SparkConf()
      .setMaster("local[*]")
      .setAppName("Test app")
      .set("spark.driver.bindAddress", "127.0.0.1") // todo: remove it later
    //    val sc = new SparkContext(config)

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    val session = SparkSession.builder()
      .config(config)
      .appName("test")
      .master("local")
      .getOrCreate()

    val training = session.read
      .format("csv")
      .option("header", "true")
      .load("file:///C:/Users/the_art_of_war/IdeaProjects/twitter-classifier/src/main/resources//train.csv")
    val df = training.withColumn("Sentiment", training.col("Sentiment").cast(IntegerType))
    //df.show(20)


    val Array(train, test) = df.randomSplit(Array[Double](0.7, 0.3))

    val all = df.toDF("ItemID","label","SentimentText")

    all.show(20)

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

//    val model = pipeline.fit(dfs)
//    println("###" * 10)
//    println(model)
//    println("###" * 10)
//    val observations = model.transform(test.toDF("ItemID", "Sentiment", "SentimentText"))
//
//    val predictionLabelsRDD = observations.select("prediction", "Sentiment").rdd.map { r =>
//      val a = java.lang.Double.parseDouble(r.get(0).toString)
//      val b = java.lang.Double.parseDouble(r.get(1).toString)
//      (a * 1.0, b * 1.0)
//    }
//    val metrics = new BinaryClassificationMetrics(predictionLabelsRDD)
//
//    val precision = metrics.precisionByThreshold

//    println("\n\n\n\n\n\n\n\n\n\n\n")
//    precision.foreach { case (t, p) =>
//      println(s"Threshold: $t, Precision: $p")
//    }
//    println("\n\n\n\n\n\n\n\n\n\n\n")

   // val temp = config.longAccumulator("counter")
//
//    val counter = SparkContext.accumulator(0)
//    predictionLabelsRDD.foreach {
//      x => counter.add( (if (Math.abs(x._1 - x._2 ) < 0.5 ) 1 else 0) )
//    }
//
//    val accuracy_value = counter.value / predictionLabelsRDD.count();
//
//    println("\n\n\n\n\n\n\n\n\n\n\n")
//    println("Counter " + counter.value)
//    println("Accuracy " + accuracy_value)
//    println("\n\n\n\n\n\n\n\n")

    val paramGrid = new ParamGridBuilder()
      .addGrid(hashingTF.numFeatures, Array(10, 100, 1000))
      .addGrid(lr.regParam, Array(0.1, 0.01))
      .build() // Get 3 by 2 grid with 6 parameter pairs to evaluate

    val cv = new CrossValidator()
      .setEstimator(pipeline) // provide your pipeline
      .setEvaluator(new BinaryClassificationEvaluator)
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(3) // Use 3+ in practice
      .setParallelism(2) // Evaluate up to 2 parameter settings in parallel

    val cvModel = cv.fit(all);

    println("*\n*\n*\n*\n*\n*\n*\n")
    val x = cvModel.getEstimatorParamMaps
      .zip(cvModel.avgMetrics)
      .maxBy(_._2)
      ._1;
    print(x)
    println("*\n*\n*\n*\n*\n*\n*\n")

    session.stop()
  }
}


