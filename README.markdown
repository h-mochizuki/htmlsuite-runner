htmlsuite-runner
======================
SeleniumServer�ŕ�����HTML�e�X�g�X�C�[�g�����s���邽�߂̊g���A�v���ł��B
 
�g����
------
### ���s���@ ###
�R�}���h���C�����玟�����s���Ă��������B

	./gradlew run -Pargs='[XML�p�X]'

### �ݒ�XML�\�� ###
	<?xml version="1.0" encoding="UTF-8"?>
	<suites-config>
		<browsers>*firefox</browsers><!-- �g�p�u���E�U(�C�ӁB�f�t�H���g�́u*firefox�v) -->
		<baseUrl>http://www.google.co.jp</baseUrl><!-- �u���E�U�N������URL(�K�{) -->
		<port>4444</port><!-- Selenium�T�[�o�̃|�[�g�ԍ�(�C�ӁB�f�t�H���g�́u4444�v) -->
		<timeoutInSeconds>60000</timeoutInSeconds><!-- �^�C���A�E�g�b��(�C�ӁB�f�t�H���g��180000�b) -->
		<resultDir>.</resultDir><!-- �e�X�g���ʏo�̓t�H���_(�C�ӁB�f�t�H���g�̓J�����g�f�B���N�g��) -->
		<multiWindow>false</multiWindow><!-- ���s���̃E�B���h�E����(�C�ӁB�f�t�H���g�́ufalse�v) -->
		<suites>
			<suite>test-suite1.html</suite><!-- �e�X�g�X�C�[�g�̃t�@�C���p�X(�K�{) -->
			<suite>test-suite2.html</suite>
		</suites>
	</suites-config>
