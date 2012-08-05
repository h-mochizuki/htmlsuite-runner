package htmlsuite

import groovy.util.slurpersupport.GPathResult

import org.openqa.selenium.server.RemoteControlConfiguration

/**
 * HTMLSuiteRunner���s�p�̐ݒ�ł��B
 * @author hidetoshi.mochizuki
 */
class HtmlSuiteRunnerConfiguration extends RemoteControlConfiguration {
	
	static final String DEFAULT_RESULT_DIR = '.'
	static final String DEFAULT_BROWSERS = '*firefox'

	String browsers = DEFAULT_BROWSERS
	String baseUrl
	boolean multiWindow = false
	String resultDir = DEFAULT_RESULT_DIR
	List<HtmlSuite> suites = []

	/**
	 * baseUrl�̕K�{�`�F�b�N��ɒl���Z�b�g����B
	 * @param url baseUrl
	 */
	void setBaseUrl(String url) {
		if (url == null || url.size() == 0) {
			throw new IllegalArgumentException("'baseUrl' is required!")
		}
		baseUrl = url
	}

	/**
	 * resultDir�̕K�{�`�F�b�N��ɒl���Z�b�g����B<p>
	 * �t�H���_�����݂��Ȃ��ꍇ�͍쐬����B
	 * @param path resultDir
	 */
	void setResultDir(String path) {
		if (path == null || path.size() == 0) {
			throw new IllegalArgumentException("'resultDir' is required!")
		}
		File dir = new File(path)
		if (!dir.exists()) {
			dir.mkdirs()
		}
		if (dir.isFile()) {
			throw new IllegalArgumentException("'${path}' is not directory!")
		}
		resultDir = path
	}

	/**
	 * HtmlSuiteRunner�̐ݒ�t�@�C����ǂݍ��݂܂��B
	 * @param xml �ݒ�t�@�C��
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	static HtmlSuiteRunnerConfiguration loadFile(File xml) {
		if (xml == null || !xml.exists() || xml.isDirectory()) {
			throw new IllegalArgumentException("Configuration file is not found! : '${xml?.absolutePath}'")
		}
		new HtmlSuiteRunnerConfiguration().parse(new XmlSlurper().parse(xml))
	}

	/**
	 * HtmlSuiteRunner�̐ݒ�t�@�C���p�X��ǂݍ��݂܂��B
	 * @param xmlPath �ݒ�t�@�C���p�X
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	static HtmlSuiteRunnerConfiguration load(String xmlPath) {
		if (xmlPath == null || xmlPath.size() == 0) {
			throw new IllegalArgumentException("Configuration file is required!")
		}
		loadFile(new File(xmlPath))
	}

	/**
	 * HtmlSuiteRunner��XML�������ǂݍ��݂܂��B
	 * @param xml �ݒ�XML
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	static HtmlSuiteRunnerConfiguration loadText(String xml) {
		new HtmlSuiteRunnerConfiguration().parse(new XmlSlurper().parseText(xml))
	}

	/**
	 * HtmlSuiteRunner�ݒ���e���烂�f���𐶐����܂��B
	 * @param xml �ݒ�
	 * @return {@link HtmlSuiteRunnerConfiguration}
	 */
	protected static HtmlSuiteRunnerConfiguration parse(GPathResult xml) {
		def conf = new HtmlSuiteRunnerConfiguration()
		// ���ʐݒ�
		conf.port = xml.port?.text() ? xml.port.text() as Integer : conf.port
		conf.browsers = xml.browsers?.text() ?: conf.browsers
		conf.baseUrl = xml.baseUrl?.text()
		conf.timeoutInSeconds = toLong(conf.timeoutInSeconds, xml.timeoutInSeconds?.text())
		conf.resultDir = xml.resultDir?.text() ?: conf.resultDir
		conf.multiWindow = ('true' == xml.multiWindow?.text())

		// HTMLSuite�ݒ�
		def count = 0
		xml.suites.suite.each  { suite ->

			// ���� > �v�f > ���ʐݒ� �̏��ɗD��
			String baseUrl = suite.@baseUrl.text() ?: suite.baseUrl?.text() ?: conf.baseUrl
			String suiteFile = suite.@suiteFile.text() ?: suite.suiteFile?.text() ?: suite.text()
			String resultFile = suite.@resultFile.text() ?: suite.resultFile?.text() ?: conf.resultDir
			long timeoutInSeconds = toLong(conf.timeoutInSeconds, suite.@timeoutInSeconds.text(), suite.timeoutInSeconds?.text())
			boolean multiWindow = toBool(conf.multiWindow, suite.@multiWindow.text(), suite.multiWindow?.text())

			// �u���E�U�ʂɃX�C�[�g���f�����쐬
			String browsers = suite.@browsers.text() ?: suite.browsers?.text() ?: conf.browsers
			browsers.split(',').findAll {!it.trim()?.isEmpty()}.each { browser ->
				conf.suites << new HtmlSuite(
					browser: browser,
					baseUrl: baseUrl,
					suiteFile: new File(suiteFile),
					resultFile: new File(resultFile),
					timeoutInSeconds: timeoutInSeconds,
					multiWindow: multiWindow,
					)
			}
		}

		if (conf.suites.isEmpty()) {
			throw new IllegalArgumentException("'suites' is required!")
		}

		return conf
	}

	/**
	 * �����񂪐ݒ肳��Ă���� long �ɂ��ĕԂ��B
	 * @param defVal �����l
	 * @param args �ϊ��Ώۂ̔z��(�D��x��)
	 * @return long �ɕϊ����ꂽ������
	 */
	static long toLong(long defVal, Object... args) {
		def result = defVal
		args.each {
			if (it) {
				result = it as Long
			}
		}
		return result
	}

	static boolean toBool(boolean defVal, Object... args) {
		def result = defVal
		args.each {
			if (it) {
				result = 'true' == it
			}
		}
		return result

	}
}
