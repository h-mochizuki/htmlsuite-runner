package htmlsuite

import org.openqa.selenium.server.htmlrunner.HTMLTestResults

/**
 * HTML�e�X�g�X�C�[�g����ێ����郂�f��
 * 
 * @author hidetoshi.mochizuki
 */
class HtmlSuite {

	private static final String PASS_RESULT = 'PASSED'
	private static final String RESULT_FILE_PREFIX = 'result-'

	String browser
	String baseUrl
	File suiteFile
	File resultFile
	long timeoutInSeconds
	boolean multiWindow = false
	HTMLTestResults suiteResult
	boolean passed = false

	/**
	 * suiteFile ��ݒ肷��B
	 * @param file suiteFile
	 */
	void setSuiteFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("'suiteFile' is required!")
		}
		if (file.exists() && file.isFile()) {
			suiteFile = file.absoluteFile
		} else {
			throw new FileNotFoundException("'${file.absolutePath}' is not found!")
		}
	}

	/**
	 * resultFile ��ݒ肷��B
	 * @param file resultFile
	 */
	void setResultFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("'resultFile' is required!")
		}
		if (file.isDirectory()) {
			String suite = suiteFile?.name ?: ''
			if (suite.size() == 0) {
				throw new IllegalArgumentException("'suiteFile' is required!")
			}
			// �ꉞ�t�H���_������Ă���
			file.mkdirs()
			file = new File(file, RESULT_FILE_PREFIX + suite)
		}
		if (!file.exists()) {
			file.createNewFile()
		}
		resultFile = file.absoluteFile
	}

	/**
	 * HTMLLauncher�̎��s���ʂ�ݒ肵�܂��B
	 * @param resultMsg HTMLLauncher�̎��s����
	 */
	void setResult(String resultMsg) {
		passed = PASS_RESULT == resultMsg
	}
}
