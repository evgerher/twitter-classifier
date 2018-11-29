trait Logger {
//  @transient private lazy val log = LogFactory.getLog(this.getClass)
  def logDebug(message: String): Unit = println(message)
  def logInfo(message: String): Unit = println(message)
  def logError(message: String): Unit = println(message)
  def logError(message: String, throwable: Throwable): Unit = println(message, throwable)
}
