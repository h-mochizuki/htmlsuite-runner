/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ci.selenium.suites.html.category

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.runtime.metaclass.MixinInstanceMetaProperty

/**
 * DSLを読み込み、モデルオブジェクトに自動反映するためのCategoryクラス<p>
 * 対応させたいモデルクラスにMixinして使用します。<br>
 * 次のようなモデルクラスを作成した場合、
 * <pre>
 * {@code @Mixin}(DSLLoadCategory)
 * class Developer {
 *   String name
 *   List<String> tasks
 * }
 * </pre>
 * 想定されるDSLはこのようになります。
 * <pre>
 * developer {
 *   name 'John Doe'
 *   tasks 'Very dull task.', 'Important task.'
 * }
 * </pre>
 * 
 * @author hidetoshi.mochizuki
 */
@Category(Object)
class DSLLoadCategory {

	/**
	 * DSLが記述されているファイルを読み込みモデルオブジェクトに反映します
	 * @param dslFile DSLファイル
	 * @return モデルオブジェクト
	 */
	def loadDSL(File dslFile) {
		loadDSL(dslFile.text)
	}

	/**
	 * DSLスクリプトを読み込みモデルオブジェクトに反映します
	 * @param dslScript DSLスクリプトが記述された文字列
	 * @return モデルオブジェクト
	 */
	def loadDSL(String dslScript) {
		def self = this
		Script script = new GroovyShell().parse(dslScript)
		script.metaClass = createEMC(script.class) { ExpandoMetaClass emc ->
			String root = instanceName(self.class)
			emc."$root" = { Closure cl -> entrustedWith(cl) }
		}
		script.run()
		return self
	}

	/**
	 * DSL内部で{@link Closure}が入れ子になっているような場合に、
	 * <code>Closure</code>と対応するモデルクラスの反映ルールを対応付けます。
	 * @param cl 入れ子構造になっている<code>Closure</code>
	 */
	void entrustedWith(Closure cl) {
		cl.delegate = delegator(this)
		cl.resolveStrategy = Closure.DELEGATE_FIRST
		cl()
	}

	private static def delegator(def self) {
		def delegate = new Object()
		self.metaClass.properties.findAll{ isDelegatableProperty(it) }.each { prop ->
			if (Collection.isAssignableFrom(prop.type)) {
				delegate.metaClass."$prop.name" = { Object... args -> self."$prop.name" = args }
			} else {
				delegate.metaClass."$prop.name" = { self."$prop.name" = it }
			}
		}
		addDSLRules(self, delegate)
		return delegate
	}

	private static boolean isDelegatableProperty(MetaBeanProperty prop) {
		!(prop instanceof MixinInstanceMetaProperty) && prop.setter
	}

	private static void addDSLRules(def self, def delegate) {
		self.metaClass.methods.findAll{ hasAdditionalDSLRule(it) }.each { MetaMethod method ->
			delegate.metaClass."$method.name" = {
				self."$method.name"(it)
			}
		}
	}

	private static boolean hasAdditionalDSLRule(def method) {
		method instanceof CachedMethod && method.cachedMethod.getAnnotation(AdditonalDSLRule)
	}

	private static ExpandoMetaClass createEMC(Class<?> clazz, Closure cl) {
		ExpandoMetaClass emc = new ExpandoMetaClass(clazz);
		cl(emc)
		emc.initialize()
		return emc
	}

	private static String instanceName(Class<?> clazz) {
		def name = clazz.simpleName
		return name.substring(0, 1).toLowerCase() + name.substring(1)
	}
}

/**
 * プロパティと1対1で対応できないDSLの追加設定を行うためのマーカーアノテーション
 * @author hidetoshi.mochizuki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface AdditonalDSLRule {
}
