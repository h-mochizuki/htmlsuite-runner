package ci.selenium.suites.html

import java.util.logging.Logger;

import groovy.util.logging.Commons;

import org.openqa.selenium.server.SeleniumServer
import org.openqa.selenium.server.htmlrunner.HTMLLauncher

import ci.selenium.suites.html.model.SuiteConfiguration;
import ci.selenium.suites.html.model.TestsConfiguration

/**
 * テストサーバを起動し、HTMLテストスイートを実行します。
 * @author hidetoshi.mochizuki
 */
class SuiteLauncher  extends HTMLLauncher {

	// ロガー
	private static Logger log = Logger.getLogger(SuiteLauncher.class.name);
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
		boolean doExecute = true
		try {
			// 既に起動している可能性があるため、 最初に停止を行う。
			doStop(testsConfig.port)
			remoteControl.start()
			// テスト実施前処理
			doExecute = chainClosure(testsConfig.beforeTests, testsConfig)
			if (doExecute) {
				// テスト実施
				testsConfig.suites.each { suite ->
					passed &= doTest(suite)
				}
			} else {
				log.info("beforeTest により、テスト実施がキャンセルされました。")
			}
		} finally {
			try {
				// テスト実施後処理
				if (doExecute) {
					chainClosure(testsConfig.afterTests, testsConfig)
				}
			} finally {
			}
			// テストが終わったら停止する
			remoteControl.stop()
		}
		return passed
	}

	protected boolean doTest(SuiteConfiguration suite) {
		// 初期設定
		results = null
		// スイート実行前処理
		if (chainClosure(suite.setUps, suite)) {
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
		} else {
			log.info("setUp により、テストスイート実行がキャンセルされました。")
		}

		// スイート実行後処理
		chainClosure(suite.tearDowns, suite)
		return suite.passed
	}

	protected static  def chainClosure(List<Closure> cls, args) {
		boolean passed = true
		cls?.each { Closure cl ->
			if (passed) {
				passed &= cl(args) as Boolean
			}
		}
		return passed
	}

	void doStop(port) {
		try {
			new URL("http://localhost:${port}/selenium-server/driver/?cmd=shutDownSeleniumServer").getText()
		} catch (Exception e) {
			// SeleniumServerが既に停止している
		}
	}
}
