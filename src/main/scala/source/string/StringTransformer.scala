package source.string

trait StringTransformer {

  object MatchDataType {

    final val numberFormat = raw"[0-9]+".r
    final val floatFormat = raw"[0-9]+\\.[0-9]+".r
    final val dateMonthFormat = raw"[0-9]{4}-[0-9]{2}".r
    final val dateFullFormat = raw"[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}+".r

  }

  implicit class MatchDataType(s: String) {

    import MatchDataType._

    def matchDataType: Any =
      s match {
        case numberFormat() => s.toString.toInt
        case floatFormat() => s.toString.toFloat
        case dateMonthFormat() => new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s + "-01")
        case dateFullFormat() => new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s)
        case _ => s.trim()
      }
  }

}

