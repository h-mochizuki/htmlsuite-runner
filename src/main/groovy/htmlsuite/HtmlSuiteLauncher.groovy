package htmlsuite

import org.openqa.selenium.server.SeleniumServer
import org.openqa.selenium.server.htmlrunner.HTMLLauncher

/**
 * HTMLSuite�����s���đS�̂̌��ʂ�ԋp���܂��B
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteLauncher {

	private static final String TEST_URL_PATH = "../tests/"
	HtmlSuiteRunnerConfiguration configuration
	int suitesCnt = 0
	int passCnt = 0

	static boolean runSuites(HtmlSuiteRunnerConfiguration config) {
		return new HtmlSuiteLauncher(configuration: config).executeSuites()
	}

	/**
	 * �e�X�g�X�C�[�g�����s���đS�̂̌��ʂ�Ԃ��܂��B
	 * @param server SeleniumServer
	 * @param suites �e�X�g�X�C�[�g�̃��X�g
	 * @return
	 */
	boolean executeSuites() {
		def server = new SeleniumServer(configuration)
		def suites = configuration.suites
		suitesCnt = suites.size()
		boolean passed = true
		try {
			server.start()
			def launcher = new HTMLLauncher(server)
			suites.each { suite ->
				launcher.setResults(null)
				server.addNewStaticContent(suite.suiteFile.parentFile)
				String suiteUrl = TEST_URL_PATH + URLEncoder.encode(suite.suiteFile.name, 'UTF-8')
				suite.setResult(launcher.runHTMLSuite(
						suite.browser,
						suite.baseUrl,
						suiteUrl,
						suite.resultFile,
						suite.timeoutInSeconds,
						suite.multiWindow))
				suite.setSuiteResult(launcher.getResults())
				if (suite.passed) {
					passCnt++
				} else {
					passed = false
				}
			}
		} finally {
			server.stop()
		}
		return passed
	}
}
