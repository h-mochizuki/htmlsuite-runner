import static org.openqa.selenium.Platform.WINDOWS;
import ci.selenium.suites.html.model.TestsConfiguration;

testsConfiguration {
	browser '*googlechrome'
	baseURL 'http://www.google.com'
	beforeTest {
		if (org.openqa.selenium.Platform.getCurrent().is(org.openqa.selenium.Platform.WINDOWS)) {
			it.browser = "*firefox"
		}
	}
	suite 'src/test/resources/sample-suite1.html'
	suite {
		suiteFile 'src/test/resources/sample-suite2.html'
		resultFile './result-suite2.html'
	}
}
