package ci.selenium.suites.html

import org.openqa.selenium.server.SeleniumServer
import org.openqa.selenium.server.htmlrunner.HTMLLauncher

import ci.selenium.suites.html.model.SuiteConfiguration;
import ci.selenium.suites.html.model.TestsConfiguration

/**
 * テストサーバを起動し、HTMLテストスイートを実行します。
 * @author hidetoshi.mochizuki
 */
class SuiteLauncher  extends HTMLLauncher {

	/** テスト実行時のスイートファイル配置URL */
	private static final String TEST_URL_PATH = "../tests/"
	/** テストサーバ */
	private SeleniumServer remoteControl
	/** テスト設定情報 */
	private TestsConfiguration testsConfig

	SuiteLauncher(TestsConfiguration config) {
		this(new SeleniumServer(config), config)
	}

	SuiteLauncher(SeleniumServer server, TestsConfiguration config) {
		super(server)
		remoteControl = server
		testsConfig = config
	}

	boolean test() {
		// テストスイート全体のテスト結果
		boolean passed = true
		try {
			// 既に起動している可能性があるため、 最初に停止を行う。
			doStop()
			remoteControl.start()
			// テスト実施前処理
			testsConfig.beforeTests.each { Closure cl -> cl.call(testsConfig) }
			// テスト実施
			testsConfig.suites.each { suite ->
				passed &= doTest(suite)
			}
		} finally {
			try {
				// テスト実施後処理
				testsConfig.afterTests.each { Closure cl -> cl.call(testsConfig) }
			} finally {
			}
			// テストが終わったら停止する
			doStop()
		}
		return passed
	}

	protected boolean doTest(SuiteConfiguration suite) {
		// 初期設定
		results = null
		// スイート実行前処理
		suite.setUps.each { Closure cl -> cl.call(suite) }

		// テストスイート実行
		remoteControl.addNewStaticContent(suite.suiteFile.parentFile)
		String suiteURL = TEST_URL_PATH + URLEncoder.encode(suite.suiteFile.name, 'UTF-8')
		suite.setResult(runHTMLSuite(
				suite.browser,
				suite.baseURL,
				suiteURL,
				suite.resultFile,
				suite.timeoutInSeconds,
				!suite.singleWindow), getResults())

		// スイート実行後処理
		suite.tearDowns.each { Closure cl -> cl.call(suite) }
		return suite.passed
	}

	void doStop() {
		remoteControl.stop()
	}
}
