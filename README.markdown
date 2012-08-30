htmlsuite-runner
======================
SeleniumServerで複数のHTMLテストスイートを実行するための拡張アプリです。
 
使い方
------
### 実行方法 ###
Linuxの場合

	./htmlsuite-runner.sh <DSLファイルパス>

Windowsの場合

	./htmlsuite-runner.bat <DSLファイルパス>

DSLファイルパスを指定しない場合は「./sampleConfiguration.groovy」が読込まれます。

### 設定ファイル構造 及び 設定例 ###
	testsConfiguration {

		// == 任意項目(デフォルト値があるため、設定しなくても動く) ==
		// サーバポート番号
		port 4444
		// テストで使用するブラウザ
		browser '*firefox'
		// テスト実施時の初期URL
		baseURL 'http://www.google.com'
		// テストスイート1つの実施上限時間(超えると強制終了)
		timeoutInSeconds 1800
		// テスト結果格納先ディレクトリ
		resultDir '.'
		// 管理ウィンドウとテスト実行ウィンドウを一体化するかどうか(GoogleChromeでは無効？)
		singleWindow false
		// テスト実施前に行いたい処理
		// it として TestsConfiguration が渡される。
		// エラーが発生した場合、後続処理をスキップする。
		beforeTest {
			println "start - ${new Date().format('yyyyMMddHHmmss')}"
		}
		// テスト実施後に行いたい処理
		// it として TestsConfiguration が渡される。
		afterTest {
			println "finish - ${new Date().format('yyyyMMddHHmmss')}"
		}
		// 追加したい Jar ファイル
		addJars 'path/to/jars'

		// == 必須項目(指定しないとテストが動かない) ==
		// テストスイートファイル(スイートファイルのパスのみ設定可能)
		suite 'src/test/resources/sample-suite1.html'

		// テストスイート設定(スイートごとに設定が可能)
		suite {
			// ブラウザ
			browser = '*googlechrome'
			// テストスイートファイルパス
			suiteFile 'src/test/resources/sample-suite2.html'
			// 結果ファイルパス
			resultFile './result-suite2.html'
			// テストスイート実施前に行いたい処理
			// it として TestsConfiguration が渡される。
			// エラーが発生した場合、スイートの実施をスキップする。
			setUp {
				println "$it.suiteFile.name を実行します。"
			}
			// テストスイート実施後に行いたい処理
			// it として TestsConfiguration が渡される。
			setUp {
				println "$it.suiteFile.name を実行しました。"
			}
		}
	}

ライセンス
----------
Copyright &copy; 2012-2012
Licensed under the [Apache License, Version 2.0][Apache]
 
[Apache]: http://www.apache.org/licenses/LICENSE-2.0
