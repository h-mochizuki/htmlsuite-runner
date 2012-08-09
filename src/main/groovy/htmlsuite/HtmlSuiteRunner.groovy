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
			result = new HtmlSuiteLauncher(conf).doTest()
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) {
				println 'Please enter the filePath argument.'
			}
			e.printStackTrace()
		}
		System.exit(result ? 0 : 1)
	}

}
