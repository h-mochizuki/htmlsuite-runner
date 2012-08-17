testsConfiguration {
	browser '*googlechrome'
	baseURL 'http://www.google.com'
	suite 'src/test/resources/sample-suite1.html'
	suite {
		suiteFile 'src/test/resources/sample-suite2.html'
		resultFile './result-suite2.html'
	}
}
