import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark._
import org.apache.spark.ml.classification.{LinearSVC, LogisticRegression}
import org.apache.spark.ml.feature.{HashingTF, LabeledPoint, Tokenizer}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.sql.{SparkSession}
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}

import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}

import org.apache.spark.sql.DataFrame
import org.apache.spark.mllib.evaluation.MulticlassMetrics

import org.apache.spark.ml.classification.NaiveBayes


import org.apache.spark.ml.classification.{GBTClassificationModel, GBTClassifier}




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
    //  val df = training.withColumn("Sentiment", training.col("Sentiment").cast(DoubleType))
    //df.show(20)


    val Array(train, test) = df.randomSplit(Array[Double](0.7, 0.3), seed = 28)

    val all = df.toDF("ItemID","label","SentimentText")

    all.show(20)



    val train_df = train.toDF("ItemID", "label", "SentimentText")
    val test_df = test.toDF("ItemID", "label", "SentimentText")

    /* find best model using CV
    val findModel = new MLFindModel()
    findModel.findBestParamsOfBayes(train_df,test_df)
    */

    // train and answer query
    val model = new Model()
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



  /* Main model for training
  *   Before it, need to create config, session
  *  read data
  * */
  class Model{
    /*
       This function need to have datafrane to train model on
       it should have columns "ItemID","label","SentimentText"
       maybe without id
     */
    def train(train_data : DataFrame): Unit = {
      val tokenizer = new Tokenizer()
        .setInputCol("SentimentText")
        .setOutputCol("Variants")

      val hashingTF = new HashingTF()
        .setNumFeatures(5000)
        .setInputCol(tokenizer.getOutputCol)
        .setOutputCol("features")

      val lsvc = new LinearSVC()
        .setMaxIter(5)
        .setRegParam(0.01)

      val pipeline = new Pipeline()
        .setStages(Array(tokenizer, hashingTF, lsvc))

      val model = pipeline.fit(train_data);
      val path = "Model"
      val local_path = "file:///C:/Users/the_art_of_war/IdeaProjects/twitter-classifier/model"
      model.write.overwrite().save(local_path);
      println("OK! saved model")
    }

    /*
       This function need to have datafrane to test model on
       it should have columns "ItemID","SentimentText"
       maybe without id
       should be the same as for train but without label
       answer in column 'predicition'
     */
    def get(test : DataFrame) : DataFrame =  {
      val path = "Model"
      val local_path = "file:///C:/Users/the_art_of_war/IdeaProjects/twitter-classifier/model"
      val model = PipelineModel.load(local_path)
      val observations = model.transform(test)
      println("OK!")
      return observations
    }

  }

  class MLFindModel{
    def findBestParamsOfLogistic(train: DataFrame, test : DataFrame) : Unit = {
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

      val paramGrid = new ParamGridBuilder()
        .addGrid(hashingTF.numFeatures, Array(1000,2000,5000,10000))
        .addGrid(lr.regParam, Array(0.1, 0.01,1.0))
        .build() // Get 3 by 2 grid with 6 parameter pairs to evaluate

      val cv = new CrossValidator()
        .setEstimator(pipeline) // provide your pipeline
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(3) // Use 3+ in practice
        .setParallelism(4) // Evaluate up to 2 parameter settings in parallel

      val cvModel = cv.fit(train);

      println("Best params for model")
      val bestParams = cvModel.getEstimatorParamMaps
        .zip(cvModel.avgMetrics)
        .maxBy(_._2)
        ._1;
      print(bestParams)
      println("")

      val observations = cvModel.transform(test)

      val predictionLabelsRDD = observations.select("prediction", "label").rdd.map { r =>
        val a = java.lang.Double.parseDouble(r.get(0).toString)
        val b = java.lang.Double.parseDouble(r.get(1).toString)
        (a * 1.0, b * 1.0)
      }
      val metrics = new BinaryClassificationMetrics(predictionLabelsRDD)

      val precision = metrics.precisionByThreshold

      val recall = metrics.recallByThreshold

      val metrics1 = new MulticlassMetrics(predictionLabelsRDD)

      println("\n\n\n\n\n")
      precision.foreach { case (t, p) =>
        println(s"Threshold: $t, Precision: $p")
      }
      recall.foreach { case (t, p) =>
        println(s"Threshold: $t, Recall : $p")
      }
      println("Accuracy : "+ metrics1.accuracy)

      println("\n\n\n\n\n")
    }

    def findBestParamsOfGradientBoosting(train: DataFrame, test : DataFrame) : Unit = {
      val tokenizer = new Tokenizer()
        .setInputCol("SentimentText")
        .setOutputCol("Variants")

      val hashingTF = new HashingTF()
        .setNumFeatures(1000)
        .setInputCol(tokenizer.getOutputCol)
        .setOutputCol("features")

      val gbt = new GBTClassifier()
        .setMaxIter(10)

      val pipeline = new Pipeline()
        .setStages(Array(tokenizer, hashingTF, gbt))

      val paramGrid = new ParamGridBuilder()
        .addGrid(hashingTF.numFeatures, Array(100,1000))
        .addGrid(gbt.maxIter, Array(1,2,5))
        .build() // Get 3 by 2 grid with 6 parameter pairs to evaluate

      val cv = new CrossValidator()
        .setEstimator(pipeline) // provide your pipeline
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(3) // Use 3+ in practice
        .setParallelism(4) // Evaluate up to 2 parameter settings in parallel

      val cvModel = cv.fit(train);

      println("Best params for model")
      val bestParams = cvModel.getEstimatorParamMaps
        .zip(cvModel.avgMetrics)
        .maxBy(_._2)
        ._1;
      print(bestParams)
      println("")

      val observations = cvModel.transform(test)

      val predictionLabelsRDD = observations.select("prediction", "label").rdd.map { r =>
        val a = java.lang.Double.parseDouble(r.get(0).toString)
        val b = java.lang.Double.parseDouble(r.get(1).toString)
        (a * 1.0, b * 1.0)
      }
      val metrics = new BinaryClassificationMetrics(predictionLabelsRDD)

      val precision = metrics.precisionByThreshold

      val recall = metrics.recallByThreshold

      val metrics1 = new MulticlassMetrics(predictionLabelsRDD)

      println("\n\n\n\n\n")
      precision.foreach { case (t, p) =>
        println(s"Threshold: $t, Precision: $p")
      }
      recall.foreach { case (t, p) =>
        println(s"Threshold: $t, Recall : $p")
      }
      println("Accuracy : "+ metrics1.accuracy)

      println("\n\n\n\n\n")
    }

    def findBestParamsOfSVM(train: DataFrame, test : DataFrame) : Unit = {
      val tokenizer = new Tokenizer()
        .setInputCol("SentimentText")
        .setOutputCol("Variants")

      val hashingTF = new HashingTF()
        .setNumFeatures(1000)
        .setInputCol(tokenizer.getOutputCol)
        .setOutputCol("features")

      val lsvc = new LinearSVC()
        .setMaxIter(10)
        .setRegParam(0.1)

      val pipeline = new Pipeline()
        .setStages(Array(tokenizer, hashingTF, lsvc))

      val paramGrid = new ParamGridBuilder()
        .addGrid(hashingTF.numFeatures, Array(1000,5000))
        .addGrid(lsvc.regParam, Array(0.1, 0.01,1.0))
        .addGrid(lsvc.maxIter, Array(5, 10))
        .build() // Get 3 by 2 grid with 6 parameter pairs to evaluate

      val cv = new CrossValidator()
        .setEstimator(pipeline) // provide your pipeline
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(3) // Use 3+ in practice
        .setParallelism(4) // Evaluate up to 2 parameter settings in parallel

      val cvModel = cv.fit(train);

      println("Best params for model")
      val bestParams = cvModel.getEstimatorParamMaps
        .zip(cvModel.avgMetrics)
        .maxBy(_._2)
        ._1;
      print(bestParams)
      println("")

      val observations = cvModel.transform(test)

      val predictionLabelsRDD = observations.select("prediction", "label").rdd.map { r =>
        val a = java.lang.Double.parseDouble(r.get(0).toString)
        val b = java.lang.Double.parseDouble(r.get(1).toString)
        (a * 1.0, b * 1.0)
      }
      val metrics = new BinaryClassificationMetrics(predictionLabelsRDD)

      val precision = metrics.precisionByThreshold

      val recall = metrics.recallByThreshold

      val metrics1 = new MulticlassMetrics(predictionLabelsRDD)

      println("\n\n\n\n\n")
      precision.foreach { case (t, p) =>
        println(s"Threshold: $t, Precision: $p")
      }
      recall.foreach { case (t, p) =>
        println(s"Threshold: $t, Recall : $p")
      }
      println("Accuracy : "+ metrics1.accuracy)

      println("\n\n\n\n\n")
    }

    def findBestParamsOfForest(train: DataFrame, test : DataFrame) : Unit = {
      val tokenizer = new Tokenizer()
        .setInputCol("SentimentText")
        .setOutputCol("Variants")

      val hashingTF = new HashingTF()
        .setNumFeatures(1000)
        .setInputCol(tokenizer.getOutputCol)
        .setOutputCol("features")

      val rf = new RandomForestClassifier()
        .setNumTrees(10)

      val pipeline = new Pipeline()
        .setStages(Array(tokenizer, hashingTF, rf))

      val paramGrid = new ParamGridBuilder()
        .addGrid(hashingTF.numFeatures, Array(1000,5000))
        .addGrid(rf.numTrees, Array(10,5,20))
        .build() // Get 3 by 2 grid with 6 parameter pairs to evaluate

      val cv = new CrossValidator()
        .setEstimator(pipeline) // provide your pipeline
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(3) // Use 3+ in practice
        .setParallelism(4) // Evaluate up to 2 parameter settings in parallel

      val cvModel = cv.fit(train);

      println("Best params for model")
      val bestParams = cvModel.getEstimatorParamMaps
        .zip(cvModel.avgMetrics)
        .maxBy(_._2)
        ._1;
      print(bestParams)
      println("")

      val observations = cvModel.transform(test)

      val predictionLabelsRDD = observations.select("prediction", "label").rdd.map { r =>
        val a = java.lang.Double.parseDouble(r.get(0).toString)
        val b = java.lang.Double.parseDouble(r.get(1).toString)
        (a * 1.0, b * 1.0)
      }
      val metrics = new BinaryClassificationMetrics(predictionLabelsRDD)

      val precision = metrics.precisionByThreshold

      val recall = metrics.recallByThreshold

      val metrics1 = new MulticlassMetrics(predictionLabelsRDD)

      println("\n\n\n\n\n")
      precision.foreach { case (t, p) =>
        println(s"Threshold: $t, Precision: $p")
      }
      recall.foreach { case (t, p) =>
        println(s"Threshold: $t, Recall : $p")
      }
      println("Accuracy : "+ metrics1.accuracy)

      println("\n\n\n\n\n")
    }

    def findBestParamsOfBayes(train: DataFrame, test : DataFrame) : Unit = {
      val tokenizer = new Tokenizer()
        .setInputCol("SentimentText")
        .setOutputCol("Variants")

      val hashingTF = new HashingTF()
        .setNumFeatures(1000)
        .setInputCol(tokenizer.getOutputCol)
        .setOutputCol("features")

      val nb = new NaiveBayes()

      val pipeline = new Pipeline()
        .setStages(Array(tokenizer, hashingTF, nb))

      val paramGrid = new ParamGridBuilder()
        .addGrid(hashingTF.numFeatures, Array(1000,5000))
        .build() // Get 3 by 2 grid with 6 parameter pairs to evaluate

      val cv = new CrossValidator()
        .setEstimator(pipeline) // provide your pipeline
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(3) // Use 3+ in practice
        .setParallelism(4) // Evaluate up to 2 parameter settings in parallel

      val cvModel = cv.fit(train);

      println("Best params for model")
      val bestParams = cvModel.getEstimatorParamMaps
        .zip(cvModel.avgMetrics)
        .maxBy(_._2)
        ._1;
      print(bestParams)
      println("")

      val observations = cvModel.transform(test)

      val predictionLabelsRDD = observations.select("prediction", "label").rdd.map { r =>
        val a = java.lang.Double.parseDouble(r.get(0).toString)
        val b = java.lang.Double.parseDouble(r.get(1).toString)
        (a * 1.0, b * 1.0)
      }
      val metrics = new BinaryClassificationMetrics(predictionLabelsRDD)

      val precision = metrics.precisionByThreshold

      val recall = metrics.recallByThreshold

      val metrics1 = new MulticlassMetrics(predictionLabelsRDD)

      println("\n\n\n\n\n")
      precision.foreach { case (t, p) =>
        println(s"Threshold: $t, Precision: $p")
      }
      recall.foreach { case (t, p) =>
        println(s"Threshold: $t, Recall : $p")
      }
      println("Accuracy : "+ metrics1.accuracy)

      println("\n\n\n\n\n")
    }

  }


}
