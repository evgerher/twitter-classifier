package classifier

import java.net.URL


object ModelLoader {
  def getResourcePath(resource: String): String = {
    return this.getClass.getResource(resource).getPath
  }

  def getModelFolder(dataset: String): String = {
    val res: String = this.getClass.getResource(s"${dataset}/train.csv").getPath
    val dir: String = res.substring(0, res.lastIndexOf("/")) + "/model"
    return dir
  }
}
