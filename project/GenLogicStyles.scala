/*
* Copyright 2001-2011 Artima, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.io.{FileWriter, BufferedWriter, File}

import scala.io.Source

object GenLogicStyles {

  val generatorSource = new File("GenLogicStyles.scala")

  def translateLine(traitName: String)(line: String): String =
    line.replaceAllLiterally("with TestRegistration", "with LogicTestRegistration")
        .replaceAllLiterally("with org.scalatest.fixture.TestRegistration", "with org.scalatest.fixture.LogicTestRegistration")
        .replaceAllLiterally("Any /* Assertion */", "org.scalatest.Assertion")
        .replaceAllLiterally(traitName, "Logic" + traitName)
        .replaceAllLiterally("AnyLogic", "Logic")
        .replaceAllLiterally("Resources.concurrentLogic" + traitName + "Mod", "Resources.concurrent" + traitName + "Mod")
        .replaceAllLiterally("Resources.concurrentFixtureLogic" + traitName + "Mod", "Resources.concurrentFixture" + traitName + "Mod")
        .replaceAllLiterally("@Finders(Array(\"org.scalatest.finders.Logic" + traitName + "Finder\"))", "@Finders(Array(\"org.scalatest.finders." + traitName + "Finder\"))")

  def translateTestLine(traitName: String)(line: String): String =
    line.replaceAllLiterally(traitName, "Logic" + traitName)
        .replaceAllLiterally("/* ASSERTION_SUCCEED */", "succeed")


  def translateTestLineNew(traitName: String)(line: String): String =
    line.replaceAllLiterally(traitName, "Logic" + traitName)
    .replaceAllLiterally("/* ASSERTION_SUCCEED */", "succeed")
    .replaceAllLiterally("new Logic" + traitName, "new " + traitName.toLowerCase + ".Logic" + traitName)
    .replaceAllLiterally("new fixture.Logic" + traitName, "new " + traitName.toLowerCase + ".FixtureLogic" + traitName)
    .replaceAllLiterally("extends Logic" + traitName, "extends " + traitName.toLowerCase + ".Logic" + traitName)
    .replaceAllLiterally("extends org.scalatest.fixture.Logic" + traitName, "extends " + traitName.toLowerCase + ".FixtureLogic" + traitName)
    .replaceAllLiterally("this: Logic" + traitName + " =>", "this: " + traitName.toLowerCase + ".Logic" + traitName + " =>")

  def translateFile(targetDir: File, fileName: String, sourceFileName: String, scalaVersion: String, scalaJS: Boolean, translateFun: String => String): File = {
    val outputFile = new File(targetDir, fileName)
    if (!outputFile.exists || generatorSource.lastModified > outputFile.lastModified) {
      val outputWriter = new BufferedWriter(new FileWriter(outputFile))
      try {
        val lines = Source.fromFile(new File(sourceFileName)).getLines.toList
        var skipMode = false
        for (line <- lines) {
          val mustLine: String =
            if (scalaJS) {
              if (line.trim == "// SKIP-SCALATESTJS,NATIVE-START") {
                skipMode = true
                ""
              }
              else if (line.trim == "// SKIP-SCALATESTJS,NATIVE-END") {
                skipMode = false
                ""
              }
              else if (!skipMode) {
                if (line.trim.startsWith("//SCALATESTJS,NATIVE-ONLY "))
                  translateFun(line.substring(line.indexOf("//SCALATESTJS,NATIVE-ONLY ") + 26))
                else
                  translateFun(line)
              }
              else
                ""
            }
            else
              translateFun(line)

          outputWriter.write(mustLine)
          outputWriter.newLine()
        }
      }
      finally {
        outputWriter.flush()
        outputWriter.close()
        println("Generated " + outputFile.getAbsolutePath)
      }
    }
    outputFile
  }

  def genMainImpl(targetDir: File, version: String, scalaVersion: String, scalaJS: Boolean): Seq[File] = {
    targetDir.mkdirs()

    val fixtureDir = new File(targetDir, "fixture")
    fixtureDir.mkdirs()

    val funsuiteDir = new File(targetDir, "funsuite")
    funsuiteDir.mkdirs()

    val featurespecDir = new File(targetDir, "featurespec")
    featurespecDir.mkdirs()

    Seq(
      translateFile(targetDir, "LogicTestRegistration.scala", "scalatest/src/main/scala/org/scalatest/TestRegistration.scala", scalaVersion, scalaJS, translateLine("TestRegistration")),

      translateFile(funsuiteDir, "LogicFunSuiteLike.scala", "scalatest/src/main/scala/org/scalatest/funsuite/AnyFunSuiteLike.scala", scalaVersion, scalaJS, translateLine("FunSuite")),
      translateFile(funsuiteDir, "LogicFunSuite.scala", "scalatest/src/main/scala/org/scalatest/funsuite/AnyFunSuite.scala", scalaVersion, scalaJS, translateLine("FunSuite")),

      translateFile(targetDir, "LogicFeatureSpecLike.scala", "scalatest/src/main/scala/org/scalatest/featurespec/AnyFeatureSpecLike.scala", scalaVersion, scalaJS, translateLine("FeatureSpec")),
      translateFile(targetDir, "LogicFeatureSpec.scala", "scalatest/src/main/scala/org/scalatest/featurespec/AnyFeatureSpec.scala", scalaVersion, scalaJS, translateLine("FeatureSpec")),

      translateFile(targetDir, "LogicFlatSpecLike.scala", "scalatest/src/main/scala/org/scalatest/FlatSpecLike.scala", scalaVersion, scalaJS, translateLine("FlatSpec")),
      translateFile(targetDir, "LogicFlatSpec.scala", "scalatest/src/main/scala/org/scalatest/FlatSpec.scala", scalaVersion, scalaJS, translateLine("FlatSpec")),

      translateFile(targetDir, "LogicFreeSpecLike.scala", "scalatest/src/main/scala/org/scalatest/FreeSpecLike.scala", scalaVersion, scalaJS, translateLine("FreeSpec")),
      translateFile(targetDir, "LogicFreeSpec.scala", "scalatest/src/main/scala/org/scalatest/FreeSpec.scala", scalaVersion, scalaJS, translateLine("FreeSpec")),

      translateFile(targetDir, "LogicFunSpecLike.scala", "scalatest/src/main/scala/org/scalatest/funspec/AnyFunSpecLike.scala", scalaVersion, scalaJS, translateLine("FunSpec")),
      translateFile(targetDir, "LogicFunSpec.scala", "scalatest/src/main/scala/org/scalatest/funspec/AnyFunSpec.scala", scalaVersion, scalaJS, translateLine("FunSpec")),

      translateFile(targetDir, "LogicPropSpecLike.scala", "scalatest/src/main/scala/org/scalatest/PropSpecLike.scala", scalaVersion, scalaJS, translateLine("PropSpec")),
      translateFile(targetDir, "LogicPropSpec.scala", "scalatest/src/main/scala/org/scalatest/PropSpec.scala", scalaVersion, scalaJS, translateLine("PropSpec")),

      translateFile(targetDir, "LogicWordSpecLike.scala", "scalatest/src/main/scala/org/scalatest/WordSpecLike.scala", scalaVersion, scalaJS, translateLine("WordSpec")),
      translateFile(targetDir, "LogicWordSpec.scala", "scalatest/src/main/scala/org/scalatest/WordSpec.scala", scalaVersion, scalaJS, translateLine("WordSpec")),


      translateFile(fixtureDir, "LogicTestRegistration.scala", "scalatest/src/main/scala/org/scalatest/fixture/TestRegistration.scala", scalaVersion, scalaJS, translateLine("TestRegistration")),

      translateFile(funsuiteDir, "FixtureLogicFunSuiteLike.scala", "scalatest/src/main/scala/org/scalatest/funsuite/FixtureAnyFunSuiteLike.scala", scalaVersion, scalaJS, translateLine("FunSuite")),
      translateFile(funsuiteDir, "FixtureLogicFunSuite.scala", "scalatest/src/main/scala/org/scalatest/funsuite/FixtureAnyFunSuite.scala", scalaVersion, scalaJS, translateLine("FunSuite")),

      translateFile(fixtureDir, "LogicFeatureSpecLike.scala", "scalatest/src/main/scala/org/scalatest/featurespec/FixtureAnyFeatureSpecLike.scala", scalaVersion, scalaJS, translateLine("FeatureSpec")),
      translateFile(fixtureDir, "LogicFeatureSpec.scala", "scalatest/src/main/scala/org/scalatest/featurespec/FixtureAnyFeatureSpec.scala", scalaVersion, scalaJS, translateLine("FeatureSpec")),

      translateFile(fixtureDir, "LogicFlatSpecLike.scala", "scalatest/src/main/scala/org/scalatest/fixture/FlatSpecLike.scala", scalaVersion, scalaJS, translateLine("FlatSpec")),
      translateFile(fixtureDir, "LogicFlatSpec.scala", "scalatest/src/main/scala/org/scalatest/fixture/FlatSpec.scala", scalaVersion, scalaJS, translateLine("FlatSpec")),

      translateFile(fixtureDir, "LogicFreeSpecLike.scala", "scalatest/src/main/scala/org/scalatest/fixture/FreeSpecLike.scala", scalaVersion, scalaJS, translateLine("FreeSpec")),
      translateFile(fixtureDir, "LogicFreeSpec.scala", "scalatest/src/main/scala/org/scalatest/fixture/FreeSpec.scala", scalaVersion, scalaJS, translateLine("FreeSpec")),

      translateFile(fixtureDir, "LogicFunSpecLike.scala", "scalatest/src/main/scala/org/scalatest/funspec/FixtureAnyFunSpecLike.scala", scalaVersion, scalaJS, translateLine("FunSpec")),
      translateFile(fixtureDir, "LogicFunSpec.scala", "scalatest/src/main/scala/org/scalatest/funspec/FixtureAnyFunSpec.scala", scalaVersion, scalaJS, translateLine("FunSpec")),

      translateFile(fixtureDir, "LogicPropSpecLike.scala", "scalatest/src/main/scala/org/scalatest/fixture/PropSpecLike.scala", scalaVersion, scalaJS, translateLine("PropSpec")),
      translateFile(fixtureDir, "LogicPropSpec.scala", "scalatest/src/main/scala/org/scalatest/fixture/PropSpec.scala", scalaVersion, scalaJS, translateLine("PropSpec")),

      translateFile(fixtureDir, "LogicWordSpecLike.scala", "scalatest/src/main/scala/org/scalatest/fixture/WordSpecLike.scala", scalaVersion, scalaJS, translateLine("WordSpec")),
      translateFile(fixtureDir, "LogicWordSpec.scala", "scalatest/src/main/scala/org/scalatest/fixture/WordSpec.scala", scalaVersion, scalaJS, translateLine("WordSpec"))
    )
  }

  def genMain(targetDir: File, version: String, scalaVersion: String): Seq[File] = {
    genMainImpl(targetDir, version, scalaVersion, false)
  }

  def genMainForScalaJS(targetDir: File, version: String, scalaVersion: String): Seq[File] = {
    genMainImpl(targetDir, version, scalaVersion, true)
  }

  def genTestImpl(targetDir: File, version: String, scalaVersion: String, scalaJS: Boolean): Seq[File] = {
    targetDir.mkdirs()

    val fixtureDir = new File(targetDir, "fixture")
    fixtureDir.mkdirs()

    Seq(
      translateFile(targetDir, "LogicFunSuiteSpec.scala", "scalatest-test/src/test/scala/org/scalatest/FunSuiteSpec.scala", scalaVersion, scalaJS, translateTestLineNew("FunSuite")),
      translateFile(targetDir, "LogicFunSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/FunSpecSpec.scala", scalaVersion, scalaJS, translateTestLineNew("FunSpec")),
      translateFile(targetDir, "LogicFunSpecSuite.scala", "scalatest-test/src/test/scala/org/scalatest/FunSpecSuite.scala", scalaVersion, scalaJS, translateTestLineNew("FunSpec")),
      translateFile(targetDir, "LogicFeatureSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/FeatureSpecSpec.scala", scalaVersion, scalaJS, translateTestLineNew("FeatureSpec")),
      translateFile(targetDir, "LogicFlatSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/FlatSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("FlatSpec")),
      translateFile(targetDir, "LogicFreeSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/FreeSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("FreeSpec")),
      translateFile(targetDir, "LogicPropSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/PropSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("PropSpec")),
      translateFile(targetDir, "LogicWordSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/WordSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("WordSpec")),

      translateFile(fixtureDir, "LogicFunSuiteSpec.scala", "scalatest-test/src/test/scala/org/scalatest/fixture/FunSuiteSpec.scala", scalaVersion, scalaJS, translateTestLineNew("FunSuite")),
      translateFile(fixtureDir, "LogicFunSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/fixture/FunSpecSpec.scala", scalaVersion, scalaJS, translateTestLineNew("FunSpec")),
      translateFile(fixtureDir, "LogicFeatureSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/fixture/FeatureSpecSpec.scala", scalaVersion, scalaJS, translateTestLineNew("FeatureSpec")),
      translateFile(fixtureDir, "LogicFlatSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/fixture/FlatSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("FlatSpec")),
      translateFile(fixtureDir, "LogicFreeSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/fixture/FreeSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("FreeSpec")),
      translateFile(fixtureDir, "LogicPropSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/fixture/PropSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("PropSpec")),
      translateFile(fixtureDir, "LogicWordSpecSpec.scala", "scalatest-test/src/test/scala/org/scalatest/fixture/WordSpecSpec.scala", scalaVersion, scalaJS, translateTestLine("WordSpec"))
    )
  }

  def genTest(targetDir: File, version: String, scalaVersion: String): Seq[File] = {
    genTestImpl(targetDir, version, scalaVersion, false)
  }
}
