package htmlsuite

/**
 * Selenium-serverを起動し、テストを実行します。
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteRunner {

	/**
	 * Seleniumテストを実行します。
	 * @param args XMLファイルパス
	 */
	static main(args) {
		def result = false
		try {
			String xmlPath = args.size() > 0 ? args[0] : null
			def conf = HtmlSuiteRunnerConfiguration.load(xmlPath)
			result = HtmlSuiteLauncher.runSuites(conf)
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) {
				println '================='
				println 'Please enter the xml file-path argument.'
				println '\tex) ./gradle run -Pargs="/dir/file.xml"'
				println '================='
			}
			e.printStackTrace()
		}
		System.exit(result ? 0 : 1)
	}

}
