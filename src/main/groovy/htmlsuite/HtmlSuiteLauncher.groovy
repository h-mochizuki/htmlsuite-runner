package htmlsuite

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer
import org.openqa.selenium.server.htmlrunner.HTMLLauncher

/**
 * HTMLSuiteを実行して全体の結果を返却します。
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteLauncher extends HTMLLauncher {

	static Logger logger = Logger.getLogger(HtmlSuiteLauncher.class.getName());
	private static final String TEST_URL_PATH = "../tests/"
	private List<Closure> setupCls = []
	private List<Closure> teardownCls = []
	private SeleniumServer remoteControl
	private List<HtmlSuite> htmlSuites
	HtmlSuite currentSuite

	HtmlSuiteLauncher(HtmlSuiteRunnerConfiguration config) {
		this(new SeleniumServer(config), config.suites)
	}

	HtmlSuiteLauncher(SeleniumServer server, List<HtmlSuite> suites) {
		super(server)
		remoteControl = server
		htmlSuites = suites
	}

	/**
	 * テストスイートを実行して全体の結果を返します。
	 * @param server SeleniumServer
	 * @param suites テストスイートのリスト
	 * @return true:全テスト成功 false:テスト失敗あり
	 */
	boolean doTest() {
		boolean passed = true
		try {
			remoteControl.start()
			htmlSuites.eachWithIndex { suite, index ->
				// 初期設定
				results = null
				currentSuite = suite
				// 事前処理を実行
				setupCls.each { it.call(suite) }
				String suiteFileName = suite.suiteFile.name

				try {
					// テスト実行
					remoteControl.addNewStaticContent(suite.suiteFile.parentFile)
					String suiteUrl = TEST_URL_PATH + URLEncoder.encode(suiteFileName, 'UTF-8')
					suite.setResult(runHTMLSuite(
							suite.browser,
							suite.baseUrl,
							suiteUrl,
							suite.resultFile,
							suite.timeoutInSeconds,
							!suite.singleWindow))
				} catch (Exception e) {
					logger.log(Level.WARNING, "Exception at testsuite : '${suiteFileName}'", e);
				} finally {
					suite.setSuiteResult(getResults())
					if (!suite.passed) {
						passed = false
					}
					teardownCls.each { it.call(suite) }
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Server exception : ", e);
		} finally {
			remoteControl.stop()
		}
		return passed
	}

	HtmlSuiteLauncher setup(Closure closure) {
		setupCls << closure
		return this
	}

	HtmlSuiteLauncher teardown(Closure closure) {
		teardownCls << closure
		return this
	}
}
