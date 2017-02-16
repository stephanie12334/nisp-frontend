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

package uk.gov.hmrc.nisp.views

import java.util.UUID

import builders.NationalInsuranceTaxYearBuilder
import org.joda.time.{LocalDate, LocalDateTime}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.nisp.config.ApplicationConfig
import uk.gov.hmrc.nisp.controllers.StatePensionController
import uk.gov.hmrc.nisp.controllers.connectors.CustomAuditConnector
import uk.gov.hmrc.nisp.helpers._
import uk.gov.hmrc.nisp.models
import uk.gov.hmrc.nisp.models._
import uk.gov.hmrc.nisp.models.enums.Exclusion
import uk.gov.hmrc.nisp.services.{CitizenDetailsService, MetricsService, NationalInsuranceService, StatePensionService}
import uk.gov.hmrc.nisp.views.html.HtmlSpec
import uk.gov.hmrc.play.frontend.auth.AuthenticationProviderIds
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.partials.CachedStaticHtmlPartialRetriever
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.views.formatting.Dates
import uk.gov.hmrc.time.DateTimeUtils.now

import scala.concurrent.Future

class StatePensionViewSpec extends UnitSpec with MockitoSugar with HtmlSpec with BeforeAndAfter with OneAppPerSuite {


  implicit val cachedStaticHtmlPartialRetriever = MockCachedStaticHtmlPartialRetriever

  val mockUserNino = TestAccountBuilder.regularNino
  val mockUserNinoExcluded = TestAccountBuilder.excludedAll
  val mockUserNinoNotFound = TestAccountBuilder.blankNino
  val json = s"test/resources/$mockUserNino.json"
  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername
  val mockUserIdExcluded = "/auth/oid/mockexcludedall"
  val mockUserIdContractedOut = "/auth/oid/mockcontractedout"
  val mockUserIdBlank = "/auth/oid/mockblank"
  val mockUserIdMQP = "/auth/oid/mockmqp"
  val mockUserIdForecastOnly =  "/auth/oid/mockforecastonly"
  val mockUserIdWeak =  "/auth/oid/mockweak"
  val mockUserIdAbroad = "/auth/oid/mockabroad"
  val mockUserIdMQPAbroad = "/auth/oid/mockmqpabroad"
  val mockUserIdFillGapsSingle = "/auth/oid/mockfillgapssingle"
  val mockUserIdFillGapsMultiple = "/auth/oid/mockfillgapsmultiple"

  val ggSignInUrl = "http://localhost:9949/gg/sign-in?continue=http%3A%2F%2Flocalhost%3A9234%2Fcheck-your-state-pension%2Faccount&origin=nisp-frontend&accountType=individual"
  val twoFactorUrl = "http://localhost:9949/coafe/two-step-verification/register/?continue=http%3A%2F%2Flocalhost%3A9234%2Fcheck-your-state-pension%2Faccount&failure=http%3A%2F%2Flocalhost%3A9234%2Fcheck-your-state-pension%2Fnot-authorised"

  lazy val fakeRequest = FakeRequest()
  def authenticatedFakeRequest(userId: String) = FakeRequest().withSession(
    SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
    SessionKeys.lastRequestTimestamp -> now.getMillis.toString,
    SessionKeys.userId -> userId,
    SessionKeys.authProvider -> AuthenticationProviderIds.VerifyProviderId
  )


  "Render State Pension view with forecast only" should {

    lazy val controller = new MockStatePensionController {
      override val citizenDetailsService: CitizenDetailsService = MockCitizenDetailsService
      override val applicationConfig: ApplicationConfig = new ApplicationConfig {
        override val assetsPrefix: String = ""
        override val reportAProblemNonJSUrl: String = ""
        override val ssoUrl: Option[String] = None
        override val betaFeedbackUnauthenticatedUrl: String = ""
        override val contactFrontendPartialBaseUrl: String = ""
        override val govUkFinishedPageUrl: String = "govukdone"
        override val showGovUkDonePage: Boolean = false
        override val analyticsHost: String = ""
        override val analyticsToken: Option[String] = None
        override val betaFeedbackUrl: String = ""
        override val reportAProblemPartialUrl: String = ""
        override val citizenAuthHost: String = ""
        override val postSignInRedirectUrl: String = ""
        override val notAuthorisedRedirectUrl: String = ""
        override val identityVerification: Boolean = false
        override val ivUpliftUrl: String = "ivuplift"
        override val ggSignInUrl: String = "ggsignin"
        override val twoFactorUrl: String = "twofactor"
        override val pertaxFrontendUrl: String = ""
        override val contactFormServiceIdentifier: String = ""
        override val breadcrumbPartialUrl: String = ""
        override val showFullNI: Boolean = false
        override val futureProofPersonalMax: Boolean = false
        override val useStatePensionAPI: Boolean = true
        override val useNationalInsuranceAPI: Boolean = true
      }
      override implicit val cachedStaticHtmlPartialRetriever: CachedStaticHtmlPartialRetriever = MockCachedStaticHtmlPartialRetriever
      override val statePensionService: StatePensionService = mock[StatePensionService]
      override val nationalInsuranceService: NationalInsuranceService = mock[NationalInsuranceService]
    }

    when(controller.statePensionService.getSummary(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Right(StatePension(
        new LocalDate(2016, 4, 5),
        amounts = StatePensionAmounts(
          protectedPayment = false,
          StatePensionAmountRegular(151.71, 590.10, 7081.15),
          StatePensionAmountForecast(4, 150.71, 590.10, 7081.15),
          StatePensionAmountMaximum(4, 1, 149.71, 590.10, 7081.15),
          StatePensionAmountRegular(0, 0, 0)
        ),
        pensionAge = 67,
        new LocalDate(2020, 6, 7),
        "2019-20",
        20,
        pensionSharingOrder = false,
        currentFullWeeklyPensionAmount = 155.65
      )
      )))

    when(controller.nationalInsuranceService.getSummary(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Right(NationalInsuranceRecord(
        qualifyingYears = 11,
        qualifyingYearsPriorTo1975 = 0,
        numberOfGaps = 1,
        numberOfGapsPayable = 1,
        new LocalDate(1954, 3, 6),
        false,
        new LocalDate(2017, 4, 5),
        List(

          NationalInsuranceTaxYearBuilder("2015-16", qualifying = true, underInvestigation = false),
          NationalInsuranceTaxYearBuilder("2014-15", qualifying = false, underInvestigation = false),
          NationalInsuranceTaxYearBuilder("2013-14", qualifying = true, underInvestigation = false) /*payable = true*/
        )
      )
      )))

   lazy val result = controller.show()(authenticatedFakeRequest(mockUserIdForecastOnly))

    //val scenario = controller.statePensionService.getSummary(mockUserNino)(HeaderCarrier()).right.get.forecastScenario

    lazy val htmlAccountDoc = asDocument(contentAsString(result))

    "render page with heading  'Your State Pension' " in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>h1.heading-large" , "nisp.main.h1.title")
    }

    "render page with text  'You can get your State Pension on' " in {
      assertElemetsOwnMessage(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p" , "nisp.main.basedOn")
    }
    "render page with text  '7 june 2020' " in {
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(1)>span:nth-child(1)" , "7 June 2020.")
    }
    "render page with text  'Your forecast is' " in {
      val sMessage  = Messages("nisp.main.caveats") + " "+ Messages("nisp.is")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(1)>span:nth-child(2)" , sMessage )
    }

    "render page with text  '£150.71 a week" in {
      val sWeek  = "£150.71 " + Messages("nisp.main.week")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(2)>em" , sWeek)
    }
    "render page with text  ' £590.10 a month, £7,081.15 a year '" in {
      val sForecastAmount  =  "£590.10 "+Messages("nisp.main.month") + ", £7,081.15 " + Messages("nisp.main.year")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(3)" , sForecastAmount)
    }
    "render page with text  ' Your forcaste '" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>p:nth-child(3)" , "nisp.main.caveats")
    }
    "render page with text  ' is not a guarantee and is based on the current law '" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>ul:nth-child(4)>li:nth-child(1)" , "nisp.main.notAGuarantee")
    }
    "render page with text  ' is based on your National Insurance record up to 5 April 2016 '" in {
      assertContainsDynamicMessage(htmlAccountDoc ,"article.content__body>ul:nth-child(4)>li:nth-child(2)" , "nisp.main.isBased" ,"5 April 2016" ,null,null)
    }
    "render page with text  ' does not include any increase due to inflation '" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>ul:nth-child(4)>li:nth-child(3)", "nisp.main.inflation")
    }

    "render page with Heading  ' £155.55 is the most you can get'" in {
      val sMaxCanGet  = "£150.71 " + Messages("nisp.main.mostYouCanGet")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>h2:nth-child(5)" ,sMaxCanGet)
    }
    "render page with text  'You cannot improve your forecast any further.'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>p:nth-child(6)" ,"nisp.main.cantImprove")
    }
    "render page with text  'If you’re working you may still need to pay National Insurance contributions until 7 June 2020 as they fund other state benefits and the NHS.'" in {
      assertContainsDynamicMessage(htmlAccountDoc ,"article.content__body>p:nth-child(7)" ,"nisp.main.context.reachMax.needToPay" , "7 June 2020" ,null ,null)
    }
    "render page with link  'View your National Insurence Record'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>a:nth-child(8)" ,"nisp.main.showyourrecord")
    }
    "render page with href link  'View your National Insurence Record'" in {
      assertLinkHasValue(htmlAccountDoc ,"article.content__body>a:nth-child(8)" ,"/check-your-state-pension/account/nirecord")
    }

    /*overseas message*/
    "render page with text  'As you are living or working overseas (opens in new tab), you may be entitled to a " +
      "State Pension from the country you are living or working in.'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>div.panel-indent:nth-child(9)>p" ,"nisp.main.overseas")
    }
    /*Ends*/

    "render page with heading  'Putting of claiming'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>h2:nth-child(10)" ,"nisp.main.puttingOff")
    }

    "render page with text  'When you are 67, you can put off claiming your State Pension. Doing this may mean you get extra State Pension when you do come to claim it. " +
      "The extra amount, along with your State Pension, forms part of your taxable income.'" in {
      assertContainsDynamicMessage(htmlAccountDoc ,"article.content__body>p:nth-child(11)" ,"nisp.main.puttingOff.line1","67" ,null ,null)
    }

    "render page with link 'More on putting off claiming (opens in new tab)'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>a:nth-child(12)" ,"nisp.main.puttingOff.linkTitle")
    }
    "render page with href link 'More on putting off claiming (opens in new tab)'" in {
      assertLinkHasValue(htmlAccountDoc ,"article.content__body>a:nth-child(12)" ,"https://www.gov.uk/deferring-state-pension")
    }

    /*Side bar help*/
    "render page with heading  'Get help'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>h2" , "nisp.nirecord.helpline.getHelp")
    }
    "render page with text  'Helpline 0345 608 0126'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>p:nth-child(2)" , "nisp.nirecord.helpline.number")
    }
    "render page with text  'Monday to Friday: 8am to 6pm'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>p:nth-child(3)" , "nisp.nirecord.helpline.openTimes")
    }
    "render page with text  'Calls cost up to 12p a minute from landlines. Calls from mobiles may cost more.'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>p:nth-child(4)" , "nisp.nirecord.helpline.callsCost")
    }
    "render page with help text 'Get help with this page.' " in {
      assertElementContainsText(htmlAccountDoc ,"div.report-error>a#get-help-action" , "Get help with this page.")
    }

  }

  "Render State Pension view with MQP : Continue Working || Less than 10 years || Fill Gaps || Full Rate" should {

    lazy val controller = new MockStatePensionController {
      override val citizenDetailsService: CitizenDetailsService = MockCitizenDetailsService
      override val applicationConfig: ApplicationConfig = new ApplicationConfig {
        override val assetsPrefix: String = ""
        override val reportAProblemNonJSUrl: String = ""
        override val ssoUrl: Option[String] = None
        override val betaFeedbackUnauthenticatedUrl: String = ""
        override val contactFrontendPartialBaseUrl: String = ""
        override val govUkFinishedPageUrl: String = "govukdone"
        override val showGovUkDonePage: Boolean = false
        override val analyticsHost: String = ""
        override val analyticsToken: Option[String] = None
        override val betaFeedbackUrl: String = ""
        override val reportAProblemPartialUrl: String = ""
        override val citizenAuthHost: String = ""
        override val postSignInRedirectUrl: String = ""
        override val notAuthorisedRedirectUrl: String = ""
        override val identityVerification: Boolean = false
        override val ivUpliftUrl: String = "ivuplift"
        override val ggSignInUrl: String = "ggsignin"
        override val twoFactorUrl: String = "twofactor"
        override val pertaxFrontendUrl: String = ""
        override val contactFormServiceIdentifier: String = ""
        override val breadcrumbPartialUrl: String = ""
        override val showFullNI: Boolean = false
        override val futureProofPersonalMax: Boolean = false
        override val useStatePensionAPI: Boolean = true
        override val useNationalInsuranceAPI: Boolean = true
      }
      override implicit val cachedStaticHtmlPartialRetriever: CachedStaticHtmlPartialRetriever = MockCachedStaticHtmlPartialRetriever
      override val statePensionService: StatePensionService = mock[StatePensionService]
      override val nationalInsuranceService: NationalInsuranceService = mock[NationalInsuranceService]
    }

    when(controller.statePensionService.getSummary(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Right(StatePension(
        new LocalDate(2016, 4, 5),
        amounts = StatePensionAmounts(
          protectedPayment = false,
          StatePensionAmountRegular(151.71, 590.10, 7081.15),
          StatePensionAmountForecast(4, 150.71, 590.10, 7081.15),
          StatePensionAmountMaximum(4, 1, 149.71, 590.10, 7081.15),
          StatePensionAmountRegular(0, 0, 0)
        ),
        pensionAge = 67,
        new LocalDate(2020, 6, 7),
        "2019-20",
        20,
        pensionSharingOrder = false,
        currentFullWeeklyPensionAmount = 155.65
      )
      )))

    when(controller.nationalInsuranceService.getSummary(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Right(NationalInsuranceRecord(
        qualifyingYears = 5,
        qualifyingYearsPriorTo1975 = 0,
        numberOfGaps = 1,
        numberOfGapsPayable = 1,
        new LocalDate(1954, 3, 6),
        false,
        new LocalDate(2017, 4, 5),
        List(

          NationalInsuranceTaxYearBuilder("2015-16", qualifying = true, underInvestigation = false),
          NationalInsuranceTaxYearBuilder("2014-15", qualifying = false, underInvestigation = false),
          NationalInsuranceTaxYearBuilder("2013-14", qualifying = true, underInvestigation = false) /*payable = true*/
        )
      )
      )))

   /* lazy val result = controller.show()(authenticatedFakeRequest(mockUserIdForecastOnly))

    val scenario = controller.statePensionService.getSummary(mockUserNino)(HeaderCarrier()).right.get.forecastScenario
    println("*** ENTERED 'login' ACTION ***")
    println(s"    content-type: ${scenario}")


    lazy val htmlAccountDoc = asDocument(contentAsString(result))

    "render page with heading  'Your State Pension' " in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>h1.heading-large" , "nisp.main.h1.title")
    }

    "render page with text  'You can get your State Pension on' " in {
      assertElemetsOwnMessage(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p" , "nisp.main.basedOn")
    }
    "render page with text  '7 june 2020' " in {
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(1)>span:nth-child(1)" , "7 June 2020.")
    }
    "render page with text  'Your forecast is' " in {
      val sMessage  = Messages("nisp.main.caveats") + " "+ Messages("nisp.is")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(1)>span:nth-child(2)" , sMessage )
    }

    "render page with text  '£150.71 a week" in {
      val sWeek  = "£150.71 " + Messages("nisp.main.week")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(2)>em" , sWeek)
    }
    "render page with text  ' £590.10 a month, £7,081.15 a year '" in {
      val sForecastAmount  =  "£590.10 "+Messages("nisp.main.month") + ", £7,081.15 " + Messages("nisp.main.year")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>div:nth-child(2)>p:nth-child(3)" , sForecastAmount)
    }
    "render page with text  ' Your forcaste '" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>p:nth-child(3)" , "nisp.main.caveats")
    }
    "render page with text  ' is not a guarantee and is based on the current law '" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>ul:nth-child(4)>li:nth-child(1)" , "nisp.main.notAGuarantee")
    }
    "render page with text  ' is based on your National Insurance record up to 5 April 2016 '" in {
      assertContainsDynamicMessage(htmlAccountDoc ,"article.content__body>ul:nth-child(4)>li:nth-child(2)" , "nisp.main.isBased" ,"5 April 2016" ,null,null)
    }
    "render page with text  ' does not include any increase due to inflation '" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>ul:nth-child(4)>li:nth-child(3)", "nisp.main.inflation")
    }

    "render page with Heading  ' £155.55 is the most you can get'" in {
      val sMaxCanGet  = "£150.71 " + Messages("nisp.main.mostYouCanGet")
      assertEqualsValue(htmlAccountDoc ,"article.content__body>h2:nth-child(5)" ,sMaxCanGet)
    }
    "render page with text  'You cannot improve your forecast any further.'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>p:nth-child(6)" ,"nisp.main.cantImprove")
    }
    "render page with text  'If you’re working you may still need to pay National Insurance contributions until 7 June 2020 as they fund other state benefits and the NHS.'" in {
      assertContainsDynamicMessage(htmlAccountDoc ,"article.content__body>p:nth-child(7)" ,"nisp.main.context.reachMax.needToPay" , "7 June 2020" ,null ,null)
    }
    "render page with link  'View your National Insurence Record'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>a:nth-child(8)" ,"nisp.main.showyourrecord")
    }
    "render page with href link  'View your National Insurence Record'" in {
      assertLinkHasValue(htmlAccountDoc ,"article.content__body>a:nth-child(8)" ,"/check-your-state-pension/account/nirecord")
    }

    /*overseas message*/
    "render page with text  'As you are living or working overseas (opens in new tab), you may be entitled to a " +
      "State Pension from the country you are living or working in.'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>div.panel-indent:nth-child(9)>p" ,"nisp.main.overseas")
    }
    /*Ends*/

    "render page with heading  'Putting of claiming'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>h2:nth-child(10)" ,"nisp.main.puttingOff")
    }

    "render page with text  'When you are 67, you can put off claiming your State Pension. Doing this may mean you get extra State Pension when you do come to claim it. " +
      "The extra amount, along with your State Pension, forms part of your taxable income.'" in {
      assertContainsDynamicMessage(htmlAccountDoc ,"article.content__body>p:nth-child(11)" ,"nisp.main.puttingOff.line1","67" ,null ,null)
    }

    "render page with link 'More on putting off claiming (opens in new tab)'" in {
      assertEqualsMessage(htmlAccountDoc ,"article.content__body>a:nth-child(12)" ,"nisp.main.puttingOff.linkTitle")
    }
    "render page with href link 'More on putting off claiming (opens in new tab)'" in {
      assertLinkHasValue(htmlAccountDoc ,"article.content__body>a:nth-child(12)" ,"https://www.gov.uk/deferring-state-pension")
    }

    /*Side bar help*/
    "render page with heading  'Get help'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>h2" , "nisp.nirecord.helpline.getHelp")
    }
    "render page with text  'Helpline 0345 608 0126'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>p:nth-child(2)" , "nisp.nirecord.helpline.number")
    }
    "render page with text  'Monday to Friday: 8am to 6pm'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>p:nth-child(3)" , "nisp.nirecord.helpline.openTimes")
    }
    "render page with text  'Calls cost up to 12p a minute from landlines. Calls from mobiles may cost more.'" in {
      assertEqualsMessage(htmlAccountDoc ,"aside.sidebar >div.helpline-sidebar>p:nth-child(4)" , "nisp.nirecord.helpline.callsCost")
    }
    "render page with help text 'Get help with this page.' " in {
      assertElementContainsText(htmlAccountDoc ,"div.report-error>a#get-help-action" , "Get help with this page.")
    }

  }*/



}