/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.nisp.utils

import javax.inject.Inject

import org.joda.time.LocalDate
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.i18n.MessagesApi
import uk.gov.hmrc.nisp.controllers.auth.NispUser
import uk.gov.hmrc.nisp.helpers.MockBreadcrumb
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser, Principal}
import uk.gov.hmrc.play.test.UnitSpec

class BreadcrumbSpec @Inject()(val messagesApi: MessagesApi) extends UnitSpec with OneAppPerSuite {
  val fakeRequestSP = FakeRequest("GET", "/account")
  val fakeRequestNI = FakeRequest("GET", "/account/nirecord/gaps")
  val fakeRequestVolContribution = FakeRequest("GET", "/account/nirecord/voluntarycontribs")
  val fakeRequestHowToImproveGaps = FakeRequest("GET", "/account/nirecord/gapsandhowtocheck")
  val messages = applicationMessages

  val authContext = new AuthContext(LoggedInUser("testName", None, None, None, CredentialStrength.Strong, ConfidenceLevel.L200, "test oid"),
    principal = Principal(None, Accounts()), None, None, None, None)

  val nispUser: NispUser = {
    new NispUser(authContext, Some("testName"), "testAuthProvider", Some("M"), Some(new LocalDate(1999, 12, 31)), None)
  }
  val emptyNispUser: NispUser = {
    new NispUser(authContext, None, "testAuthProvider", Some("M"), Some(new LocalDate(1999, 12, 31)), None)
  }

  "Breadcrumb utils" should {
    "return a item text as Account Home and State Pension" in {
      MockBreadcrumb.generateHeaderUrl()(fakeRequestSP, nispUser, messages) should include("Account+home")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestSP, nispUser, messages) should include("State+Pension")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestSP, nispUser, messages) should not include ("NI+record")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestSP, nispUser, messages) should not include "Voluntary+contributions"
      MockBreadcrumb.generateHeaderUrl()(fakeRequestSP, nispUser, messages) should not include "Gaps+in+your+record"
    }

    "return a breadcrumb url without a 'name' variable, when user.name has no value" in {
      MockBreadcrumb.generateHeaderUrl()(fakeRequestSP, emptyNispUser, messages) should not include ("name=")
    }

    "return a breadcrumb url with users' name, when user.name has value " in {
      MockBreadcrumb.generateHeaderUrl()(fakeRequestSP, nispUser, messages) should include("name=" + nispUser.name.get)
    }

    "return a item text as Account Home, State Pension and NI Record when URL is /account/nirecord/gaps" in {
      MockBreadcrumb.generateHeaderUrl()(fakeRequestNI, nispUser, messages) should include("Account+home")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestNI, nispUser, messages) should include("State+Pension")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestNI, nispUser, messages) should include("NI+record")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestNI, nispUser, messages) should not include "Voluntary+contributions"
      MockBreadcrumb.generateHeaderUrl()(fakeRequestNI, nispUser, messages) should not include "Gaps+in+your+record"
    }

    "return a item text as Account Home, State Pension and NI Record when URL is /account/nirecord/voluntarycontribs" in {
      MockBreadcrumb.generateHeaderUrl()(fakeRequestVolContribution, nispUser, messages) should include("Account+home")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestVolContribution, nispUser, messages) should include("State+Pension")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestVolContribution, nispUser, messages) should include("NI+record")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestVolContribution, nispUser, messages) should include("Voluntary+contributions")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestVolContribution, nispUser, messages) should not include "Gaps+in+your+record"
    }

    "return a item text as Account Home, State Pension and NI Record when URL is /account/nirecord/gapsandhowtocheck" in {
      MockBreadcrumb.generateHeaderUrl()(fakeRequestHowToImproveGaps, nispUser, messages) should include("Account+home")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestHowToImproveGaps, nispUser, messages) should include("State+Pension")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestHowToImproveGaps, nispUser, messages) should include("NI+record")
      MockBreadcrumb.generateHeaderUrl()(fakeRequestHowToImproveGaps, nispUser, messages) should not include "Voluntary+contributions"
      MockBreadcrumb.generateHeaderUrl()(fakeRequestHowToImproveGaps, nispUser, messages) should include("Gaps+in+your+record")
    }

    "return no items when hideBreadcrumb is set to true" in {
      MockBreadcrumb.generateHeaderUrl(hideBreadcrumb = true)(fakeRequestHowToImproveGaps, nispUser, messages) should not include ("Account+home")
      MockBreadcrumb.generateHeaderUrl(hideBreadcrumb = true)(fakeRequestHowToImproveGaps, nispUser, messages) should not include ("State+Pension")
      MockBreadcrumb.generateHeaderUrl(hideBreadcrumb = true)(fakeRequestHowToImproveGaps, nispUser, messages) should not include ("NI+record")
      MockBreadcrumb.generateHeaderUrl(hideBreadcrumb = true)(fakeRequestHowToImproveGaps, nispUser, messages) should not include "Voluntary+contributions"
      MockBreadcrumb.generateHeaderUrl(hideBreadcrumb = true)(fakeRequestHowToImproveGaps, nispUser, messages) should not include ("Gaps+in+your+record")
    }
  }
}
