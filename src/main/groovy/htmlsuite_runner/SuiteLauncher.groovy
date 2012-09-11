package htmlsuite_runner

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level
import java.util.logging.Logger

import org.openqa.selenium.server.SeleniumServer
import org.openqa.selenium.server.htmlrunner.HTMLLauncher
import org.openqa.selenium.server.htmlrunner.HTMLTestResults

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

	private SuiteLauncher(SeleniumServer server, TestsConfiguration config) {
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
			remoteControl.start()
			// テスト実施前処理
			try {
				executeClosure(testsConfig.beforeTests, testsConfig)
			} catch (Exception e) {
				throw new RuntimeException("beforeTestにてエラーが発生したため、処理を終了します。", e)
			}
			// テスト実施
			doExecute = true
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

	/**
	 * {@inheritDoc}<p>
	 * テストスイート実行結果を負荷軽減用拡張クラスにラップします。
	 * @param resultParm テストスイート実行結果
	 */
	@Override
	public void processResults(HTMLTestResults resultsParm) {
		this.results = new SuiteTestResult(resultsParm);
	}

	/**
	 * ログファイルサイズが大きい場合のCPU負荷軽減対応を行った{@link HTMLTestResults}の拡張クラスです。
	 * @author hidetoshi.mochizuki
	 */
	static class SuiteTestResult extends HTMLTestResults {

		// ロガー
		private static Logger logger = Logger.getLogger(SuiteTestResult.class.name);
		private static final HEADER = getStaticFieldTextVal('HEADER')
		private static final SUMMARY_HTML = getStaticFieldTextVal('SUMMARY_HTML')
		private static final SUITE_HTML = getStaticFieldTextVal('SUITE_HTML')
		private HTMLTestResults results

		/**
		 * {@link HTMLTestResults}クラスの<code>Static</code>フィールド値を取得します。
		 * @param fieldName フィールド名称
		 * @return <code>HTMLTestResults</code>クラスの<code>Static</code>フィールド値
		 */
		static def getStaticFieldTextVal(String fieldName) {
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

		/**
		 * {@inheritDoc}
		 * <p>
		 * 結果ファイル書き込みの際CPU負荷が高くなっていたため、
		 * 負荷を軽減するように対応しました。
		 * @param out ファイルライタ
		 */
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

		/**
		 * ログをファイルに出力します。
		 * <p>
		 * HTML特殊文字をエスケープする際にCPU負荷が高かったため、
		 * 一時ファイルに書き出してからエスケープするように対応しています。
		 * @param writer ファイルライタ
		 * @param log ログ内容
		 */
		private void writeLog(Writer writer, String log) {
			if (log != null) {
				File tmpFile = File.createTempFile("selenium_log_${new Date().format('yyyyMMddHHmmss')}", 'tmp')
				try {
					logger.info("$tmpFile.name を作成しました。")
					tmpFile.withWriter("UTF-8") { it.write(log) }
					tmpFile.eachLine {
						writer.writeLine(quoteCharacters(it))
						writer.flush()
					}
				} finally {
					logger.info("$tmpFile.name を削除しました。")
					tmpFile.delete()
				}
			}
		}

		/**
		 * HTML特殊文字列をエスケープします。
		 * <p>
		 * 全角がUTF-8文字コードとして出力されており、
		 * {@code &}が{@code &amp;}として出力されてしまったため、
		 * エスケープに制限を追加しています。
		 * @param s 対象文字列
		 * @return エスケープ後文字列
		 */
		public static String quoteCharacters(String s) {
			StringBuffer result = null;
			int max = s.length();
			int delta = 0;
			for (int i = 0; i < max; i++) {
				char c = s.charAt(i);
				String replacement = null;

				// 全角対応
				if (c == '&' && i + 1 < max && s.charAt(i + 1) != '#') {
					replacement = "&amp;";
				} else if (c == '<') {
					replacement = "&lt;";
				} else if (c == '>') {
					replacement = "&gt;";
				} else if (c == '"') {
					replacement = "&quot;";
				} else if (c == '\'') {
					replacement = "&apos;";
				}

				if (replacement != null) {
					if (result == null) {
						result = new StringBuffer(s);
					}
					result.replace(i + delta, i + delta + 1, replacement);
					delta += (replacement.length() - 1);
				}
			}
			if (result == null) {
				return s;
			}
			return result.toString();
		}
	}
}
