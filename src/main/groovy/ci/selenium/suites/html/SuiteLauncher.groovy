package ci.selenium.suites.html

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
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

	static class SuiteTestResult extends HTMLTestResults {

		// 上限を超えたらテンポラリファイルにログ書き出しを行う
		private static final long MAX_FILE_SIZE = 524288
		private static final HEADER
		private static final SUMMARY_HTML
		private static final SUITE_HTML
		private HTMLTestResults results

		static {
			HEADER = getTextVal('HEADER')
			SUMMARY_HTML = getTextVal('SUMMARY_HTML')
			SUITE_HTML = getTextVal('SUITE_HTML')
		}

		static String getTextVal(String fieldName) {
			def field = HTMLTestResults.class.getDeclaredField(fieldName)
			field.setAccessible(true)
			field.get(null)
		}

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
			this.results = results
		}

		@Override
		public void write(Writer out) throws IOException {
			// ファイルサイズが大きいときにCPUが張り付くため、
			// BufferedWriterを使用する。
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(out)
				writer.write(HEADER);
				writer.write(MessageFormat.format(SUMMARY_HTML,
						results.result,
						results.totalTime,
						results.numTestTotal,
						results.numTestPasses,
						results.numTestFailures,
						results.numCommandPasses,
						results.numCommandFailures,
						results.numCommandErrors,
						results.seleniumVersion,
						results.seleniumRevision,
						results.suite.updatedSuite))
				writer.write("<table>")
				for (int i = 0; i < testTables.size(); i++) {
					String table = testTables.get(i).replace("\u00a0", "&nbsp;")
					writer.write(MessageFormat.format(SUITE_HTML, i, results.suite.getHref(i), table))
				}
				writer.write("</table><pre>\n")
				if (results.log != null) {
					writeLog(writer, results.log)
				}
				writer.write("</pre></body></html>")
				writer.flush()
			} finally {
				if (writer != null) {
					writer.close()
				}
			}
		}

		private void writeLog(Writer writer, String log) {
			if (log != null) {
				long logSize = log.length()
				if (logSize < MAX_FILE_SIZE) {
					// ファイルサイズが上限以下なら直接書込む
					writer.write(quoteCharacters(log));
					writer.flush()
				} else {
					// 上限より大きいならテンポラリファイルに一度書込む
					File tmpFile = File.createTempFile("selenium_log_${new Date().format('yyyyMMddHHmmss')}", 'tmp')
					try {
						println("$tmpFile.name を作成しました")
						tmpFile.withWriter { it.write(log) }
						tmpFile.eachLine {
							writer.writeLine(quoteCharacters(it))
							writer.flush()
						}
					} finally {
						tmpFile.delete()
					}
				}
			}
		}


	}
}
