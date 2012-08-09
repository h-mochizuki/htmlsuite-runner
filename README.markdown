htmlsuite-runner
======================
SeleniumServerで複数のHTMLテストスイートを実行するための拡張アプリです。
 
使い方
------
### 実行方法 ###
コマンドラインから次を実行してください。

	./gradlew run -Pargs='[XMLパス]'

### 設定XML構造 ###
	<?xml version="1.0" encoding="UTF-8"?>
	<suites-config>
		<browsers>*firefox</browsers><!-- 使用ブラウザ(任意。デフォルトは「*firefox」) -->
		<baseUrl>http://www.google.co.jp</baseUrl><!-- ブラウザ起動時のURL(必須) -->
		<port>4444</port><!-- Seleniumサーバのポート番号(任意。デフォルトは「4444」) -->
		<timeoutInSeconds>60000</timeoutInSeconds><!-- タイムアウト秒数(任意。デフォルトは1800秒) -->
		<resultDir>.</resultDir><!-- テスト結果出力フォルダ(任意。デフォルトはカレントディレクトリ) -->
		<singleWindow>false</singleWindow><!-- 実行時に管理ウィンドウと一体化するか(任意。デフォルトは「false」) -->
		<suites>
			<suite>test-suite1.html</suite><!-- テストスイートのファイルパス(必須) -->
			<suite>test-suite2.html</suite>
		</suites>
	</suites-config>
