/*
 * Copyright 2012 the original author or authors.
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
package ci.selenium.suites.html.model

import spock.lang.Specification

/**
 * {@link TestsConfiguration}のテスト
 * @author hidetoshi.mochizuki
 */
class TestsConfigurationSpock extends Specification {

	def 'DSL内容が空'() {
		setup:
		def dsl = ""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		conf.port == 4444
		conf.browser == TestsConfiguration.DEFAULT_BROWSER
		conf.baseURL == TestsConfiguration.DEFAULT_BASEURL
		conf.singleWindow == false
		conf.resultDir == TestsConfiguration.DEFAULT_RESULT_DIR
		conf.suites == []
	}

	def 'DSLの要素名が不正'() {
		setup:
		def dsl = "piyo {}"

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		// piyoがない
		thrown(MissingMethodException)
	}

	def 'DSLの枠だけを読込み'() {
		setup:
		def dsl = """
			testsConfiguration {
			}
		"""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		conf.port == 4444
		conf.browser == TestsConfiguration.DEFAULT_BROWSER
		conf.baseURL == TestsConfiguration.DEFAULT_BASEURL
		conf.singleWindow == false
		conf.resultDir == TestsConfiguration.DEFAULT_RESULT_DIR
		conf.suites == []
	}

	def 'DSLの共通部分を設定'() {
		setup:
		def dsl = """
			testsConfiguration {
				port 9000
				browser '*googlechrome'
				baseURL 'http://www.yahoo.com'
				singleWindow true
				resultDir '/tmp'
			}
		"""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		conf.port == 9000
		conf.browser == '*googlechrome'
		conf.baseURL == 'http://www.yahoo.com'
		conf.singleWindow == true
		conf.resultDir == '/tmp'
		conf.suites == []
	}

	def 'Suiteにファイルパスを指定'() {
		setup:
		def dsl = """
			testsConfiguration {
				baseURL 'http://www.yahoo.com'
				suite 'src/test/resources/sample-suite1.html'
			}
		"""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		conf.port == 4444
		conf.browser == TestsConfiguration.DEFAULT_BROWSER
		conf.baseURL == 'http://www.yahoo.com'
		conf.singleWindow == false
		conf.resultDir == TestsConfiguration.DEFAULT_RESULT_DIR
		conf.suites.size() == 1
		conf.suites[0].parent == conf
		conf.suites[0].suiteFile == new File('src/test/resources/sample-suite1.html')
		conf.suites[0].resultFile ==new File( './result-sample-suite1.html')
		conf.suites[0].browser == conf.browser
		conf.suites[0].baseURL == conf.baseURL
		conf.suites[0].timeoutInSeconds == conf.timeoutInSeconds

		cleanup:
		conf.suites[0].resultFile.delete()
	}

	def 'Suiteを詳細指定'() {
		setup:
		def dsl = """
			testsConfiguration {
				baseURL 'http://www.yahoo.com'
				suite {
					browser '*googlechrome'
					baseURL 'http://localhost:8080/'
					suiteFile 'src/test/resources/sample-suite2.html'
					resultFile 'src/test/resources/sample-suite2Result.html'
				}
			}
		"""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		conf.port == 4444
		conf.browser == TestsConfiguration.DEFAULT_BROWSER
		conf.baseURL == 'http://www.yahoo.com'
		conf.singleWindow == false
		conf.resultDir == TestsConfiguration.DEFAULT_RESULT_DIR
		conf.suites.size() == 1
		conf.suites[0].parent == conf
		conf.suites[0].suiteFile == new File('src/test/resources/sample-suite2.html')
		conf.suites[0].resultFile ==new File( 'src/test/resources/sample-suite2Result.html')
		conf.suites[0].browser == '*googlechrome'
		conf.suites[0].baseURL == 'http://localhost:8080/'
		conf.suites[0].timeoutInSeconds == conf.timeoutInSeconds

		cleanup:
		conf.suites[0].resultFile.delete()
	}

	def 'Suiteに存在しないファイルパスを指定'() {
		setup:
		def dsl = """
			testsConfiguration {
				suite '存在しないファイル'
			}
		"""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		// Fileがない
		thrown(FileNotFoundException)
	}

	def 'Suiteの詳細指定に存在しないファイルパスを指定'() {
		setup:
		def dsl = """
			testsConfiguration {
				suite {
					suiteFile '存在しないファイル'
				}
			}
		"""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		// Fileがない
		thrown(FileNotFoundException)
	}

	def 'Suiteを複数指定'() {
		setup:
		def dsl = """
			testsConfiguration {
				baseURL 'http://www.yahoo.com'
				suite 'src/test/resources/sample-suite1.html'
				suite {
					browser '*googlechrome'
					baseURL 'http://localhost:8080/'
					suiteFile 'src/test/resources/sample-suite2.html'
					resultFile 'src/test/resources/sample-suite2Result.html'
				}
			}
		"""

		when:
		TestsConfiguration conf = new TestsConfiguration().loadDSL(dsl)

		then:
		conf.port == 4444
		conf.browser == TestsConfiguration.DEFAULT_BROWSER
		conf.baseURL == 'http://www.yahoo.com'
		conf.singleWindow == false
		conf.resultDir == TestsConfiguration.DEFAULT_RESULT_DIR
		conf.suites.size() == 2
		conf.suites[0].parent == conf
		conf.suites[0].suiteFile == new File('src/test/resources/sample-suite1.html')
		conf.suites[0].resultFile ==new File( './result-sample-suite1.html')
		conf.suites[0].browser == conf.browser
		conf.suites[0].baseURL == conf.baseURL
		conf.suites[0].timeoutInSeconds == conf.timeoutInSeconds
		conf.suites[1].parent == conf
		conf.suites[1].suiteFile == new File('src/test/resources/sample-suite2.html')
		conf.suites[1].resultFile ==new File( 'src/test/resources/sample-suite2Result.html')
		conf.suites[1].browser == '*googlechrome'
		conf.suites[1].baseURL == 'http://localhost:8080/'
		conf.suites[1].timeoutInSeconds == conf.timeoutInSeconds

		cleanup:
		conf.suites[0].resultFile.delete()
		conf.suites[1].resultFile.delete()
	}
}
