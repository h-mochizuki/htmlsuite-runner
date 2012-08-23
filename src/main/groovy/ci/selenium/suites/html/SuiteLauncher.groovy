package ci.selenium.suites.html

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level
import java.util.logging.Logger

import org.openqa.selenium.server.SeleniumServer
import org.openqa.selenium.server.htmlrunner.HTMLLauncher
import org.openqa.selenium.server.htmlrunner.HTMLTestResults

import ci.selenium.suites.html.model.SuiteConfiguration
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
		boolean doExecute = false
		try {
			// 既に起動している可能性があるため、 最初に停止を行う。
			doStop(testsConfig.port)
			doExecute = true
			remoteControl.start()
			// テスト実施前処理
			try {
				executeClosure(testsConfig.beforeTests, testsConfig)
			} catch (Exception e) {
				throw new RuntimeException("beforeTestにてエラーが発生したため、処理を終了します。", e)
			}
			// テスト実施
			testsConfig.suites.each { suite ->
				passed &= doTest(suite)
			}
		} finally {
			try {
				// テスト実施後処理
				if (doExecute) {
					executeClosure(testsConfig.afterTests, testsConfig)
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "afterTest にてエラーが発生しました。", e);
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
		try {
			executeClosure(suite.setUps, suite)
		} catch (Exception e) {
			log.log(Level.WARNING, "'$suite.suiteFile.name' の tearDown にてエラーが発生したため、このテストスイートをスキップします。", e);
			return
		}
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
		try {
			executeClosure(suite.tearDowns, suite)
		} catch (Exception e) {
			log.log(Level.WARNING, "'$suite.suiteFile.name' の tearDown にてエラーが発生しました。後続のスイート処理は続行されます。", e);
		}
		return suite.passed
	}

	protected static  void executeClosure(List<Closure> cls, args) {
		cls?.each { Closure cl ->
			cl(args)
		}
	}

	void doStop(port) {
		try {
			new URL("http://localhost:${port}/selenium-server/driver/?cmd=shutDownSeleniumServer").getText()
			log.info("Seleniumサーバを停止しました。");
		} catch (Exception e) {
			// SeleniumServerが既に停止している
		}
	}

	@Override
	public void processResults(HTMLTestResults resultsParm) {
		this.results = new SuiteTestResult(resultsParm);
	}

	class SuiteTestResult extends HTMLTestResults {

		public SuiteTestResult(HTMLTestResults results) {
			super(
			results.seleniumVersion,
			results.seleniumRevision,
			results.result,
			results.totalTime,
			results.numTestTotal,
			results.numTestPasses,
			results.numTestFailures,
			results.numCommandPasses,
			results.numCommandFailures,
			results.numCommandErrors,
			results.suite.updatedSuite,
			results.testTables,
			results.log)
		}

		@Override
		public void write(Writer out) throws IOException {
			// ファイルサイズが大きいときにCPUが張り付くため、
			// BufferedWriterを使用する。
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(out);
				super.write(writer);
			} finally {
				if (writer != null) {
					writer.close()
				}
			}
		}
	}
}
