/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package htmlsuite_runner

import org.openqa.selenium.server.htmlrunner.HTMLTestResults

/**
 * テストスイートの設定を保持するクラス<p>
 * @author hidetoshi.mochizuki
 */
@Mixin(DSLLoadCategory)
class SuiteConfiguration {
	/** テスト成功時にランチャーから返却される文字列 */
	private static final String PASS_RESULT = 'PASSED'
	/** テスト結果ファイルに付加される接頭語 */
	private static final String RESULT_FILE_PREFIX = 'result-'
	/** スイートを取りまとめるテスト設定 */
	final TestsConfiguration parent

	/** ブラウザ */
	String browser
	/** テスト実施の際に表示される初期URL */
	String baseURL
	/** テストスイートファイル */
	private String suiteFile
	/** テスト結果ファイル */
	private String resultFile
	/** テストスイート実行前に実施する処理のリスト */
	private List<Closure> setUps = []
	/** テストスイート実行後に実施する処理のリスト */
	private List<Closure> tearDowns = []

	/** Seleniumテスト結果情報 */
	private HTMLTestResults testResults
	/** テストスイート成功フラグ */
	private boolean passed = false

	SuiteConfiguration(TestsConfiguration parent, String suiteFile) {
		this(parent)
		this.suiteFile = suiteFile
		validateSuiteFile()
	}

	SuiteConfiguration(TestsConfiguration parent) {
		this.parent = parent
	}

	String getBrowser() {
		browser?:parent.browser
	}

	String getBaseURL() {
		baseURL?:parent.baseURL
	}

	boolean isSingleWindow() {
		parent.singleWindow
	}

	long getTimeoutInSeconds() {
		parent.timeoutInSeconds
	}

	File getSuiteFile() {
		validateSuiteFile()
		return new File(suiteFile)
	}

	File getResultFile() {
		File result = (resultFile
						? new File(resultFile)
						: new File(parent.resultDir, RESULT_FILE_PREFIX + getSuiteFile().name))
		if (!result.exists()) {
			result.parentFile.mkdirs()
			result.createNewFile()
		}
		return result
	}

	boolean isPassed() {
		passed
	}

	void setResult(String returnCd, HTMLTestResults results) {
		passed = (PASS_RESULT == returnCd)
		testResults = results
	}

	@AdditonalDSLRule
	void suiteFile(String filePath) {
		suiteFile = filePath
		validateSuiteFile()
	}

	@AdditonalDSLRule
	void resultFile(String filePath) {
		resultFile = filePath
	}

	@AdditonalDSLRule
	void setUp(String scriptPath) {
		setUps << {
			new GroovyShell().evaluate(resultFile);
		}
	}

	@AdditonalDSLRule
	void setUp(Closure cl) {
		setUps << cl
	}

	@AdditonalDSLRule
	void tearDown(Closure cl) {
		tearDown << cl
	}

	List<Closure> getSetUps() {
		new ArrayList<Closure>(setUps)
	}

	List<Closure> getTearDowns() {
		new ArrayList<Closure>(tearDowns)
	}

	private void validateSuiteFile() {
		if (!suiteFile) {
			throw new IllegalArgumentException("テストスイートファイルが指定されていません。")
		} else if (!new File(suiteFile).exists()) {
			throw new FileNotFoundException("テストスイートファイルが参照できません。 : $suiteFile")
		}
	}
}
