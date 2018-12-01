package classifier

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.LinearSVC
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.sql.DataFrame

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
