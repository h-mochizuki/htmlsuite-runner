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
package ci.selenium.suites.html.model

import org.openqa.selenium.server.RemoteControlConfiguration

import ci.selenium.suites.html.category.AdditonalDSLRule
import ci.selenium.suites.html.category.DSLLoadCategory

/**
 * テストの設定を保持するクラス<p>
 * サーバの設定も保持します。
 * @author hidetoshi.mochizuki
 */
@Mixin(DSLLoadCategory)
class TestsConfiguration extends RemoteControlConfiguration {
	
	/** デフォルトのテスト結果出力先ディレクトリパス */
	static final String DEFAULT_RESULT_DIR = '.'
	/** デフォルトで使用するブラウザ */
	static final String DEFAULT_BROWSER = '*firefox'
	/** デフォルトで表示するURL */
	static final String DEFAULT_BASEURL = 'http://www.google.com'

	/** ブラウザ */
	String browser = DEFAULT_BROWSER
	/** テスト開始時の初期URL */
	String baseURL = DEFAULT_BASEURL
	/** テスト時のウィンドウモード(singleWindowの場合、ひとつのウィンドウで管理と実行を行う) */
	boolean singleWindow = false
	/** テスト結果出力先ディレクトリ */
	String resultDir = DEFAULT_RESULT_DIR
	/** テストスイート */
	List<SuiteConfiguration> suites = []
	/** テスト実行前の処理リスト */
	private List<Closure> beforeTests = []
	/** テスト実行後の処理リスト */
	private List<Closure> afterTests = []

	@AdditonalDSLRule
	void suite(String suiteFile) {
		suites << new SuiteConfiguration(this, suiteFile)
	}

	@AdditonalDSLRule
	void suite(Closure cl) {
		SuiteConfiguration suite = new SuiteConfiguration(this)
		suites << suite
		suite.entrustedWith(cl)
	}

	@AdditonalDSLRule
	void beforeTest(Closure cl) {
		beforeTests << cl
	}
	
	@AdditonalDSLRule
	void afterTest(Closure cl) {
		afterTests << cl
	}
}
