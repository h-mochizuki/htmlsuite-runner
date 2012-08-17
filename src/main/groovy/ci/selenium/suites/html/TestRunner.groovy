package ci.selenium.suites.html

import ci.selenium.suites.html.model.TestsConfiguration

class TestRunner {

	static void main(def args) {
		String configPath = args.length > 0 ? args[0] : 'sampleConfiguration.groovy'
		TestsConfiguration config = new TestsConfiguration().loadDSL(new File(configPath))
		System.exit(new SuiteLauncher(config).test() ? 0 : 1)
	}
}
