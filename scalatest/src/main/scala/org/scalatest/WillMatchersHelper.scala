/*
 * Copyright 2001-2013 Artima, Inc.
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
package org.scalatest

import org.scalactic.Prettifier
import org.scalactic.source
import org.scalatest.exceptions.TestFailedException

private[scalatest] object WillMatchersHelper {

  def checkNoException(fun: => Any)(implicit prettifier: Prettifier): Fact = {
    try {
      fun
      Fact.Yes(Resources.noExceptionWasThrown)(prettifier)
    }
    catch {
      case u: Throwable => {
        val message = Resources.exceptionNotExpected(u.getClass.getName)
        Fact.No(message, u)(prettifier)
      }
    }
  }

  def indicateSuccess(message: => String)(implicit prettifier: Prettifier): Fact = Fact.Yes(message)(prettifier)

  def indicateSuccess(shouldBeTrue: Boolean, message: => String, negatedMessage: => String)(implicit prettifier: Prettifier): Fact =
    Fact.Yes(if (shouldBeTrue) message else negatedMessage)(prettifier)

  def indicateFailure(failureMessage: => String, optionalCause: Option[Throwable], pos: source.Position)(implicit prettifier: Prettifier): Fact = {
    Fact.No(failureMessage)(prettifier)
  }

  def indicateFailure(failureMessage: => String, optionalCause: Option[Throwable], pos: source.Position, analysis: Option[String])(implicit prettifier: Prettifier): Fact = {
    val message: String = failureMessage
    Fact.No(message)(prettifier)  // TODO: we may need to include analysis to Fact too, analysis.map(Vector(_)).getOrElse(Vector.empty)
  }

  def indicateFailure(e: TestFailedException)(implicit prettifier: Prettifier): Fact =
    Fact.No(e.getMessage)(prettifier)

}