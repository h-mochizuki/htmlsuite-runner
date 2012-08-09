package htmlsuite

import spock.lang.Specification

/**
 * HtmlSuite実行のテスト
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteLauncherSpock extends Specification {

	// テストファイル名
	def TEST_FILE = 'HtmlSuiteLauncherSpock.html'
	// テストディレクトリ名
	def TEST_DIR = 'HtmlSuiteLauncherSpock'

	def setup() {
		def suiteWr = new FileWriter('suite-' + TEST_FILE)
		suiteWr.write(testSuiteTemplate)
		suiteWr.close()
		def testWr = new FileWriter(TEST_FILE)
		testWr.write(testTemplate)
		testWr.close()
	}

	def cleanup() {
		new File('.').listFiles().findAll{it.name ==~ /.*HtmlSuiteLauncherSpock.html/}.each {
			if (it.exists()) {
				it.delete()
			}
		}
		new File(TEST_DIR).deleteDir()
	}

	def 'テキストを指定してテストスイート実行'() {
		setup:
		def result = false
		def htmlSuiteLauncher = new HtmlSuiteLauncher(
				HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<baseUrl>http://www.google.co.jp</baseUrl>
				<browsers>*firefox, *firefox</browsers>
				<suites>
					<suite>suite-${TEST_FILE}</suite>
				</suites>
			</suites-config>
"""))

		when:
		result = htmlSuiteLauncher.doTest()

		then:
		result == true
	}

	def 'ファイルを指定してテストスイート実行_setup指定'() {
		setup:
		def result = false
		def htmlSuiteLauncher = new HtmlSuiteLauncher(
				HtmlSuiteRunnerConfiguration.load('src/test/resources/sample.xml'))
		htmlSuiteLauncher.setup { println "${it.suiteFile.name}を実行します。"  }

		when:
		result = htmlSuiteLauncher.doTest()

		then:
		result == true
	}

	def 'ファイルを指定してテストスイート実行_setupで例外'() {
		setup:
		def result = false
		def htmlSuiteLauncher = new HtmlSuiteLauncher(
				HtmlSuiteRunnerConfiguration.load('src/test/resources/sample.xml'))
		htmlSuiteLauncher.setup { throw new UnsupportedOperationException() }

		when:
		result = htmlSuiteLauncher.doTest()

		then:
		thrown(UnsupportedOperationException)
	}

	// テストスイートテンプレート
	String testSuiteTemplate = """<?xml version="1.0" encoding="UTF-8"?>
		<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
			<head>
				<meta content="text/html; charset=UTF-8" http-equiv="content-type" />
				<title>Test Suite</title>
			</head>
			<body>
				<table id="suiteTable" cellpadding="1" cellspacing="1" border="1" class="selenium"><tbody>
					<tr><td><b>Test Suite</b></td></tr>
					<tr><td><a href="./${TEST_FILE}">${TEST_FILE}</a></td></tr>
				</tbody></table>
			</body>
		</html>
	"""

	// テストテンプレート
	String testTemplate = """<?xml version="1.0" encoding="UTF-8"?>
		<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
			<head profile="http://selenium-ide.openqa.org/profiles/test-case">
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
				<link rel="selenium.base" href="http://www.google.com/" />
				<title>${TEST_FILE}</title>
			</head>
			<body>
				<table cellpadding="1" cellspacing="1" border="1">
					<thead><tr><td rowspan="1" colspan="3">${TEST_FILE}</td></tr></thead>
					<tbody>
						<tr><td>open</td><td>/webhp?hl=ja</td><td></td></tr>
						<tr><td>type</td><td>id=gbqfq</td><td>google</td></tr>
						<tr><td>click</td><td>id=gbqfb</td><td></td></tr>
					</tbody>
				</table>
			</body>
		</html>
	"""

}
