package com.codacy.duplication.scala.seed

import java.io.PrintStream

import com.codacy.duplication.scala.seed.utils.FileHelper
import com.codacy.plugins.api.duplication.DuplicationClone
import play.api.libs.json.Json

class ResultsPrinter(resultsStream: PrintStream = Console.out,
                     logStream: PrintStream = Console.err,
                     isDebug: Boolean = false) {

  def log(message: String): Unit = {
    if (isDebug) {
      logStream.println(message)
    }
  }

  def logStackTrace(error: Throwable): Unit = {
    error.printStackTrace(logStream)
  }

  def printResults(results: Seq[DuplicationClone], sourcePath: String): Unit = {
    results.foreach { result =>
      val relativizedDuplicationClone =
        result.copy(
          files = result.files.map(file => file.copy(filePath = FileHelper.stripPath(file.filePath, sourcePath))))
      resultsStream.println(Json.stringify(Json.toJson(relativizedDuplicationClone)))
    }
  }
}
