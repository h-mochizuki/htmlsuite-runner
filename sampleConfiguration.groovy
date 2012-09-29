testsConfiguration {
	browser '*googlechrome'
	baseURL 'http://www.google.com'
	// 実行日時で結果フォルダを作成
	resultDir "result-${new Date().format('yyyyMMddHHmm')}"
	beforeTest {
		// ウィンドウズの場合は、ブラウザをFirefoxに変更する
		if (org.openqa.selenium.Platform.getCurrent().is(org.openqa.selenium.Platform.WINDOWS)) {
			it.browser = "*firefox"
		}
	}
	suite 'src/test/resources/sample-suite1.html'
//	suite 'src/test/resources/sample-suite2.html'
}
