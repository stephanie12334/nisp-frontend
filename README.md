[Check your State Pension] Frontend
==================================================================

[![Build Status](https://travis-ci.org/hmrc/nisp-frontend.svg?branch=master)](https://travis-ci.org/hmrc/nisp-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/nisp-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/nisp-frontend/_latestVersion)

This service provides the frontend endpoint for the [Check your State Pension] project, formally known as the National Insurance and State Pension project.

Other components:
* [State Pension Backend](https://github.com/hmrc/state-pension)
* [National Insurance Backend](https://github.com/hmrc/national-insurance-record)

Summary
---------

This service provides the following useful information to the customer:

* When they will reach [State Pension] age
* How much their [State Pension] is currently worth
* A forecast of what their [State Pension] will be when they reach State Pension age
* A view of their [National Insurance] record, including any gaps

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), and needs at least a [JRE] version 8 to run.


Authentication
------------

This customer logs into this service using [GOV.UK Verify](https://www.gov.uk/government/publications/introducing-govuk-verify/introducing-govuk-verify) and the [Government Gateway](https://www.gov.uk/government-gateway)


Acronyms
---

In the context of this application we use the following acronyms and define their
meanings. Provided you will also find a web link to discover more about the systems
and technology.

* [API]: Application Programming Interface

* [HoD]: Head of Duty

* [JRE]: Java Runtime Environment

* [JSON]: JavaScript Object Notation

* [NI]: National Insurance

* [SP]: State Pension

* [NINO]: National Insurance Number

* [URL]: Uniform Resource Locator

License
---

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

[NPS]: http://www.publications.parliament.uk/pa/cm201012/cmselect/cmtreasy/731/73107.htm
[HoD]: http://webarchive.nationalarchives.gov.uk/+/http://www.hmrc.gov.uk/manuals/sam/samglossary/samgloss249.htm
[NINO]: http://www.hmrc.gov.uk/manuals/nimmanual/nim39110.htm
[NI]: https://www.gov.uk/national-insurance/overview
[National Insurance]: https://www.gov.uk/national-insurance/overview
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator
[State Pension]: https://www.gov.uk/new-state-pension/overview
[SP]: https://www.gov.uk/new-state-pension/overview
[JSON]: http://json.org/
[Check your State Pension]: https://www.gov.uk/check-state-pension

