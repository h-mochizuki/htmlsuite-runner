package htmlsuite

import org.openqa.selenium.server.RemoteControlConfiguration

import spock.lang.Specification

/**
 * HtmlSuiteRunner�̃e�X�g
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteRunnerSpock extends Specification {

	static String TEST_FILE = 'HtmlSuiteRunnerSpock-test.html'
	static String TEST_DIR = 'HtmlSuiteRunnerSpock-test'
	def config

	// ������
	def setup() {
		new File(TEST_FILE).createNewFile()
		new File(TEST_DIR).mkdir()
	}

	// ��Еt��
	def cleanup() {
		new File('.').listFiles().findAll {it.name ==~ /.*HtmlSuiteRunnerSpock-test.html/}.each {
			if (it.exists()) {
				it.delete()
			}
		}
		new File(TEST_DIR).deleteDir()
	}

	def 'loadText baseUrl�w��Ȃ�'() {
		when:
		config = HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<suites>
					<suite>${TEST_FILE}</suite>
				</suites>
			</suites-config>
		""")

		then:
		thrown(IllegalArgumentException)
	}

	def 'loadText suites�Ȃ�'() {
		when:
		config = HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<baseUrl>http://www.google.co.jp</baseUrl>
			</suites-config>
		""")

		then:
		thrown(IllegalArgumentException)
	}

	def 'loadText suites��'() {
		when:
		config = HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<baseUrl>http://www.google.co.jp</baseUrl>
				<suites />
			</suites-config>
		""")

		then:
		thrown(IllegalArgumentException)
	}

	def 'loadText suiteFile�����݂��Ȃ�'() {
		when:
		config = HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<baseUrl>http://www.google.co.jp</baseUrl>
				<suites>
					<suite>���݂��Ȃ��񂾂�</suite>
				</suites>
			</suites-config>
		""")

		then:
		thrown(FileNotFoundException)
	}

	def 'loadText �ŏ����̒�`'() {
		when:
		config = HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<baseUrl>http://www.google.co.jp</baseUrl>
				<suites>
					<suite>${TEST_FILE}</suite><!-- ���ڎw��ł��� -->
				</suites>
			</suites-config>
		""")

		then:
		config.browsers == HtmlSuiteRunnerConfiguration.DEFAULT_BROWSERS
		config.baseUrl == 'http://www.google.co.jp'
		config.port == RemoteControlConfiguration.DEFAULT_PORT
		config.timeoutInSeconds == RemoteControlConfiguration.DEFAULT_TIMEOUT_IN_SECONDS
		config.resultDir == HtmlSuiteRunnerConfiguration.DEFAULT_RESULT_DIR
		config.multiWindow == false
		config.suites.each {
			assert it.browser == HtmlSuiteRunnerConfiguration.DEFAULT_BROWSERS
			assert it.baseUrl == 'http://www.google.co.jp'
			assert it.suiteFile.name == TEST_FILE
			assert it.resultFile.name == 'result-' + TEST_FILE
			assert it.timeoutInSeconds == RemoteControlConfiguration.DEFAULT_TIMEOUT_IN_SECONDS
			assert it.multiWindow == false
		}
	}

	def 'loadText ���ʐݒ�����ׂĎw��'() {
		when:
		config = HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<browsers>*firefox, *googlechrome</browsers>
				<baseUrl>http://www.google.co.jp</baseUrl>
				<port>8080</port>
				<timeoutInSeconds>30</timeoutInSeconds>
				<resultDir>${TEST_DIR}</resultDir>
				<multiWindow>true</multiWindow>
				<suites>
					<suite suiteFile="${TEST_FILE}" /><!-- �����Ŏw��ł��� -->
				</suites>
			</suites-config>
		""")

		then:
		config.browsers == '*firefox, *googlechrome'
		config.baseUrl == 'http://www.google.co.jp'
		config.port == 8080
		config.timeoutInSeconds == 30
		config.resultDir == TEST_DIR
		config.multiWindow == true
		config.suites.size() == 2
		config.suites.each {
			assert it.browser =~ /^(\*firefox)|(\*googlechrome)$/
			assert it.baseUrl == 'http://www.google.co.jp'
			assert it.suiteFile.name == TEST_FILE
			assert it.resultFile.name == 'result-' + TEST_FILE
			assert it.timeoutInSeconds == 30
			assert it.multiWindow == true
		}
	}

	def 'loadText suite�̐ݒ�����ׂĎw��'() {
		when:
		config = HtmlSuiteRunnerConfiguration.loadText("""<?xml version="1.0" encoding="UTF-8"?>
			<suites-config>
				<baseUrl>http://www.google.co.jp</baseUrl>
				<suites>
					<!-- �v�f�Ŏw�� -->
					<suite>
						<suiteFile>${TEST_FILE}</suiteFile>
						<resultFile>${TEST_DIR}/xxx-${TEST_FILE}</resultFile><!-- �㏑�� -->
						<browsers>*googlechrome</browsers><!-- �㏑�� -->
						<baseUrl>http://www.google.co.uk</baseUrl><!-- �㏑�� -->
						<timeoutInSeconds>0</timeoutInSeconds><!-- �㏑�� -->
						<multiWindow>true</multiWindow><!-- �㏑�� -->
					</suite>
					<!-- �����Ŏw�� -->
					<suite	suiteFile = "${TEST_FILE}"
							resultFile = "${TEST_DIR}/xxx-${TEST_FILE}"
							browsers = "*googlechrome"
							baseUrl = "http://www.google.co.uk"
							timeoutInSeconds = "0"
							multiWindow = "true"
					/>
				</suites>
			</suites-config>
		""")

		then:
		config.browsers == HtmlSuiteRunnerConfiguration.DEFAULT_BROWSERS
		config.baseUrl == 'http://www.google.co.jp'
		config.port == RemoteControlConfiguration.DEFAULT_PORT
		config.timeoutInSeconds == RemoteControlConfiguration.DEFAULT_TIMEOUT_IN_SECONDS
		config.resultDir == HtmlSuiteRunnerConfiguration.DEFAULT_RESULT_DIR
		config.multiWindow == false
		config.suites.size() == 2
		config.suites.each {
			assert it.browser == '*googlechrome'
			assert it.suiteFile.name == TEST_FILE
			assert it.resultFile.name == 'xxx-' + TEST_FILE
			assert it.baseUrl == 'http://www.google.co.uk'
			assert it.timeoutInSeconds == 0
			assert it.multiWindow == true
		}
	}

}
