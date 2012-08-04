package sample.selenium.testsuite

/**
 * @author hidetoshi.mochizuki
 *
 */
class HtmlSuiteRunner {

	static main(args) {
		def result = false
		try {
			String xmlPath = args.size() > 0 ? args[0] : null
			def conf = HtmlSuiteRunnerConfiguration.load(xmlPath)
			result = HtmlSuiteLauncher.runSuites(conf)
		} catch (Exception e) {
			e.printStackTrace()
		}
		System.exit(result ? 0 : 1)
	}

}
