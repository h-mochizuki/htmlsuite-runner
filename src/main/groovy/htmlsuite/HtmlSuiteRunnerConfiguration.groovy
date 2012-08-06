package htmlsuite

import groovy.util.slurpersupport.GPathResult

import org.openqa.selenium.server.RemoteControlConfiguration

/**
 * HTMLSuiteRunner実行用の設定です。
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteRunnerConfiguration extends RemoteControlConfiguration {
	
	static final String DEFAULT_RESULT_DIR = '.'
	static final String DEFAULT_BROWSERS = '*firefox'

	String browsers = DEFAULT_BROWSERS
	String baseUrl
	boolean multiWindow = false
	String resultDir = DEFAULT_RESULT_DIR
	List<HtmlSuite> suites = []

	/**
	 * baseUrlの必須チェック後に値をセットする。
	 * @param url baseUrl
	 */
	void setBaseUrl(String url) {
		if (url == null || url.size() == 0) {
			throw new IllegalArgumentException("'baseUrl' is required!")
		}
		baseUrl = url
	}

	/**
	 * resultDirの必須チェック後に値をセットする。<p>
	 * フォルダが存在しない場合は作成する。
	 * @param path resultDir
	 */
	void setResultDir(String path) {
		if (path == null || path.size() == 0) {
			throw new IllegalArgumentException("'resultDir' is required!")
		}
		File dir = new File(path)
		if (!dir.exists()) {
			dir.mkdirs()
		}
		if (dir.isFile()) {
			throw new IllegalArgumentException("'${path}' is not directory!")
		}
		resultDir = path
	}

	/**
	 * テストスイートの件数を返します。
	 * @return テストスイートの件数
	 */
	int getSuitesCnt() {
		return suites.size()
	}

	/**
	 * 失敗したテストスイートの件数を返します。
	 * @return 失敗したテストスイートの件数
	 */
	int getFailedCnt() {
		return suites.findAll{!it.passed && it.suiteResult}.size()
	}

	/**
	 * HtmlSuiteRunnerの設定ファイルを読み込みます。
	 * @param xml 設定ファイル
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	static HtmlSuiteRunnerConfiguration loadFile(File xml) {
		if (xml == null || !xml.exists() || xml.isDirectory()) {
			throw new IllegalArgumentException("Configuration file is not found! : '${xml?.absolutePath}'")
		}
		new HtmlSuiteRunnerConfiguration().parse(new XmlSlurper().parse(xml))
	}

	/**
	 * HtmlSuiteRunnerの設定ファイルパスを読み込みます。
	 * @param xmlPath 設定ファイルパス
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	static HtmlSuiteRunnerConfiguration load(String xmlPath) {
		if (xmlPath == null || xmlPath.size() == 0) {
			throw new IllegalArgumentException("Configuration file is required!")
		}
		loadFile(new File(xmlPath))
	}

	/**
	 * HtmlSuiteRunnerのXML文字列を読み込みます。
	 * @param xml 設定XML
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	static HtmlSuiteRunnerConfiguration loadText(String xml) {
		new HtmlSuiteRunnerConfiguration().parse(new XmlSlurper().parseText(xml))
	}

	/**
	 * HtmlSuiteRunner設定内容からモデルを生成します。
	 * @param xml 設定XML参照
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	protected static HtmlSuiteRunnerConfiguration parse(GPathResult xml) {
		def conf = new HtmlSuiteRunnerConfiguration()
		// 共通設定
		conf.port = xml.port?.text() ? xml.port.text() as Integer : conf.port
		conf.browsers = xml.browsers?.text() ?: conf.browsers
		conf.baseUrl = xml.baseUrl?.text()
		conf.timeoutInSeconds = toLong(conf.timeoutInSeconds, xml.timeoutInSeconds?.text())
		conf.resultDir = xml.resultDir?.text() ?: conf.resultDir
		conf.multiWindow = ('true' == xml.multiWindow?.text())

		// HTMLSuite設定
		def count = 0
		xml.suites.suite.each  { suite ->

			// 属性 > 要素 > 共通設定 の順に優先
			String baseUrl = suite.@baseUrl.text() ?: suite.baseUrl?.text() ?: conf.baseUrl
			String suiteFile = suite.@suiteFile.text() ?: suite.suiteFile?.text() ?: suite.text()
			String resultFile = suite.@resultFile.text() ?: suite.resultFile?.text() ?: conf.resultDir
			long timeoutInSeconds = toLong(conf.timeoutInSeconds, suite.@timeoutInSeconds.text(), suite.timeoutInSeconds?.text())
			boolean multiWindow = toBool(conf.multiWindow, suite.@multiWindow.text(), suite.multiWindow?.text())

			// ブラウザ別にスイートモデルを作成
			String browsers = suite.@browsers.text() ?: suite.browsers?.text() ?: conf.browsers
			browsers.split(',').collect{it.trim()}.findAll {!it.isEmpty()}.each { browser ->
				conf.suites << new HtmlSuite(
					browser: browser,
					baseUrl: baseUrl,
					suiteFile: new File(suiteFile),
					resultFile: new File(resultFile),
					timeoutInSeconds: timeoutInSeconds,
					multiWindow: multiWindow,
					)
			}
		}

		if (conf.suites.isEmpty()) {
			throw new IllegalArgumentException("'suites' is required!")
		}

		return conf
	}

	/**
	 * 文字列が設定されていれば long にして返す。
	 * @param defVal 初期値
	 * @param args 変換対象の配列(優先度順)
	 * @return long に変換された文字列
	 */
	static long toLong(long defVal, Object... args) {
		def result = defVal
		args.each {
			if (it) {
				result = it as Long
			}
		}
		return result
	}

	/**
	 * 文字列が設定されていれば boolean にして返す。
	 * @param defVal 初期値
	 * @param args 変換対象の配列(優先度順)
	 * @return boolean に変換された文字列
	 */
	static boolean toBool(boolean defVal, Object... args) {
		def result = defVal
		args.each {
			if (it) {
				result = 'true' == it
			}
		}
		return result
	}
}
