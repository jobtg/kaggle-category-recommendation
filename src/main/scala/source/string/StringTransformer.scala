package source.string

//import java.util.Date
//
//import scala.reflect.ClassTag
//import scala.reflect.runtime.universe
//import scala.reflect.runtime.universe.typeOf

trait StringTransformer {

  /**
    * Define all regular expressions used for matching in one object.
    */
  object MatchDataType {

    final val numberFormat = raw"[0-9]+".r
    final val floatFormat = raw"[0-9]+\\.[0-9]+".r
    final val dateMonthFormat = raw"[0-9]{4}-[0-9]{2}".r
    final val dateFullFormat = raw"[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}+".r

  }

  /**
    * Implicitly convert string to "any" other data type.
    *
    * @param s : String to match with a different data type.
    */
  implicit class MatchDataType(s: String) {

    import MatchDataType._

    /**
      * Match to data types: bool, int, float, date, string.
      *
      * @return Any
      */
    def matchDataType: Any =
      s match {
        case "N" => false
        case "Y" => true
        case numberFormat() => s.toString.toInt
        case floatFormat() => s.toString.toFloat
        case dateMonthFormat() => new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s + "-01")
        case dateFullFormat() => new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s)
        case _ => s.trim()
      }
  }

}

//trait CaseClassImporter {
//
//  object CaseClassType {
//    final val classArguments = getClassArguments
//  }
//
//  def getClassArguments[T]: Map[String, universe.Type] =
//    typeOf[T]
//      .members.filter(!_.isMethod)
//      .map(x => x.name.toString.trim -> x.info).toMap
//
//
//  def stringConvert[T](value: Any, default: T)(implicit tag: ClassTag[T]): T =
//    value match {
//      case result: T => result
//      case _ => default // or whatever you want to do if value is not a T
//    }
//
//  def parseIntoClass(args: Map[String, String]): Seq[Any] = {
//    import CaseClassType._
//
//    val argumentsParsed = args.zip(classArguments).map {
//      case (argument, varType) => stringConvert(argument._2, varType)
//    }
//  }
//
//}
