package htmlsuite

import org.openqa.selenium.server.htmlrunner.HTMLTestResults

/**
 * HTMテストスイート情報を保持するモデル
 * 
 * @author hidetoshi.mochizuki
 */
class HtmlSuite {

	private static final String PASS_RESULT = 'PASSED'
	private static final String RESULT_FILE_PREFIX = 'result-'

	String browser
	String baseUrl
	File suiteFile
	File resultFile
	long timeoutInSeconds
	boolean singleWindow
	HTMLTestResults suiteResult
	boolean passed = false

	/**
	 * suiteFile を設定する。
	 * @param file suiteFile
	 */
	void setSuiteFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("'suiteFile' is required!")
		}
		if (file.exists() && file.isFile()) {
			suiteFile = file.absoluteFile
		} else {
			throw new FileNotFoundException("'${file.absolutePath}' is not found!")
		}
	}

	/**
	 * resultFile を設定する。
	 * @param file resultFile
	 */
	void setResultFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("'resultFile' is required!")
		}
		if (file.isDirectory()) {
			String suite = suiteFile?.name ?: ''
			if (suite.size() == 0) {
				throw new IllegalArgumentException("'suiteFile' is required!")
			}
			// 一応フォルダを作っておく
			file.mkdirs()
			file = new File(file, RESULT_FILE_PREFIX + suite)
		}
		if (!file.exists()) {
			file.createNewFile()
		}
		resultFile = file.absoluteFile
	}

	/**
	 * HTMLLauncherの実行結果を設定します。
	 * @param resultMsg HTMLLauncherの実行結果
	 */
	void setResult(String resultMsg) {
		passed = PASS_RESULT == resultMsg
	}
}
