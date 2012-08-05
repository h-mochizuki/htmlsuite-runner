package htmlsuite

import org.openqa.selenium.server.SeleniumServer
import org.openqa.selenium.server.htmlrunner.HTMLLauncher

/**
 * HTMLSuiteを実行して全体の結果を返却します。
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteLauncher {

	private static final String TEST_URL_PATH = "../tests/"
	HtmlSuiteRunnerConfiguration configuration

	static boolean runSuites(HtmlSuiteRunnerConfiguration config) {
		return new HtmlSuiteLauncher(configuration: config).executeSuites()
	}

	/**
	 * テストスイートを実行して全体の結果を返します。
	 * @param server SeleniumServer
	 * @param suites テストスイートのリスト
	 * @return
	 */
	boolean executeSuites() {
		def server = new SeleniumServer(configuration)
		def suites = configuration.suites
		boolean passed = true
		try {
			server.start()
			def launcher = new HTMLLauncher(server)
			suites.each {
				suite ->
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
				if (!suite.passed) {
					passed = false
				}
			}
		} catch (Exception e) {
			e.printStackTrace()
		} finally {
			server.stop()
		}
		return passed
	}
}
