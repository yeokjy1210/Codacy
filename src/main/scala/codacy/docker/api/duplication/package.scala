package codacy.docker.api

import com.codacy.api.dtos.{Language, Languages}
import play.api.libs.json._
import codacy.docker.api.DuplicationConfiguration.{Key, Value}
import codacy.docker.api.Source.File

import scala.language.implicitConversions
import scala.util.Try

private[this] case class DuplicationConfigurationValue(value: JsValue) extends AnyVal with Value

package object duplication {

  implicit val languageFormat: Format[Language] = Format[Language](
    Reads { json: JsValue =>
      json.validate[String].flatMap { langStr =>
        Languages
          .fromName(langStr)
          .fold[JsResult[Language]](JsError(s"Could not find language for name $langStr"))(JsSuccess(_))
      }
    },
    Writes((v: Language) => JsString(v.name)))

  implicit val fileFormat: OFormat[File] =
    Json.format[File]

  implicit def configurationValueToJsValue(configValue: Value): JsValue = {
    configValue match {
      case DuplicationConfigurationValue(v) => v
      case _                                => JsNull
    }
  }

  implicit class ConfigurationExtensions(config: DuplicationConfiguration.type) {
    def Value(jsValue: JsValue): Value =
      DuplicationConfigurationValue(jsValue)

    def Value(raw: String): Value =
      Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

  implicit val configurationValueFormat: Format[Value] =
    Format(implicitly[Reads[JsValue]].map(DuplicationConfigurationValue), Writes(configurationValueToJsValue))

  implicit val configurationOptionsKeyFormat: OFormat[Key] =
    Json.format[Key]

  implicit val configurationOptionsFormat: Format[Map[Key, Value]] =
    Format[Map[Key, Value]](
      Reads { json: JsValue =>
        JsSuccess(json.asOpt[Map[String, JsValue]].fold(Map.empty[Key, Value]) {
          _.map {
            case (k, v) =>
              Key(k) -> DuplicationConfigurationValue(v)
          }
        })
      },
      Writes { m =>
        JsObject(m.flatMap {
          case (k, v: DuplicationConfigurationValue) => Option(k.value -> v.value)
          case _                                     => Option.empty[(String, JsValue)]
        })
      })

  implicit val dupCloneFileFmt: OFormat[DuplicationCloneFile] = Json.format[DuplicationCloneFile]
  implicit val dupCloneFmt: OFormat[DuplicationClone] = Json.format[DuplicationClone]
  implicit val dupCfgFmt: OFormat[DuplicationConfiguration] = Json.format[DuplicationConfiguration]
  implicit val codacyCfgFmt: OFormat[CodacyConfiguration] = Json.format[CodacyConfiguration]

}
