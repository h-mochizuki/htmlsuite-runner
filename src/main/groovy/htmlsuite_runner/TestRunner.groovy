package htmlsuite_runner

import groovy.util.logging.Commons

@Commons
class TestRunner {

	static void main(def args) {
		System.exit(new TestRunner().test(args.length > 0 ? args[0] : 'sampleConfiguration.groovy') ? 0 : 1)
	}

	boolean test(String configPath) {
		try {
			TestsConfiguration config = new TestsConfiguration().loadDSL(new File(configPath))
			return new SuiteLauncher(config).test()
		} catch (Exception e) {
			log.error("テストの実行中にエラーが発生しました。", e)
		}
		return false
	}
}
