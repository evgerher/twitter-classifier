package classifier

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification._
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.sql.DataFrame

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
