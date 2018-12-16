package source.string

class StringTransformer {

  implicit class matchDataType(s: String) {
    def matchDataType: Any = {
      val numberFormat = raw"[0-9]+".r
      val floatFormat = raw"[0-9]+\\.[0-9]+".r
      val dateMonthFormat = raw"[0-9]{4}-[0-9]{2}".r
      val dateFullFormat = raw"[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}+".r

      s match {
        case numberFormat() => s.toString.toInt
        case floatFormat() => s.toString.toFloat
        case dateMonthFormat() => new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s + "-01")
        case dateFullFormat() => new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s)
        case _ => s.trim()
      }
    }
  }

}

object StringTransformer {
  val matcher = new StringTransformer
}
