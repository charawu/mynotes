package com.v.v_notes.control
import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.StringWriter

object SettingsManager {
    private const val TAG = "SettingsManager"
    private const val FILE_NAME = "settings.xml"
    private lateinit var settingsFile: File

    // 所有设置的键名定义
    object Keys {
        const val FIXED_MENU = "fixed menu"
    }

    // 初始化，在应用启动时调用一次
    fun init(context: Context) {
        settingsFile = File(context.filesDir, FILE_NAME)
        Log.d(TAG, "设置文件路径: ${settingsFile.absolutePath}")

        // 如果文件不存在，创建默认设置文件
        if (!settingsFile.exists()) {
            createDefaultSettings()
        }
    }

    // 读取布尔值设置
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return readValue(key)?.toBooleanStrictOrNull() ?: defaultValue
    }

    // 保存布尔值设置
    fun putBoolean(key: String, value: Boolean) {
        writeValue(key, value.toString())
    }

    // 读取字符串设置
    fun getString(key: String, defaultValue: String = ""): String {
        return readValue(key) ?: defaultValue
    }

    // 保存字符串设置
    fun putString(key: String, value: String) {
        writeValue(key, value)
    }

    // 读取整数设置
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return readValue(key)?.toIntOrNull() ?: defaultValue
    }

    // 保存整数设置
    fun putInt(key: String, value: Int) {
        writeValue(key, value.toString())
    }

    // 获取所有设置
    fun getAllSettings(): Map<String, String> {
        val settings = mutableMapOf<String, String>()

        try {
            if (!settingsFile.exists()) return settings

            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            FileInputStream(settingsFile).use { input ->
                parser.setInput(input, "UTF-8")

                var eventType = parser.eventType
                var currentKey: String? = null

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name != "settings") {
                                currentKey = parser.name
                                val value = parser.getAttributeValue(null, "value")
                                if (currentKey != null && value != null) {
                                    settings[currentKey!!] = value
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取设置失败", e)
        }

        return settings
    }

    fun clear() {
        writeAllSettings(emptyMap())
    }

    // 原始XML内容
    fun getXmlContent(): String {
        return if (settingsFile.exists()) {
            settingsFile.readText()
        } else {
            "文件不存在"
        }
    }

    // 从 XML 读取值
    private fun readValue(key: String): String? {
        try {
            if (!settingsFile.exists()) return null

            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            FileInputStream(settingsFile).use { input ->
                parser.setInput(input, "UTF-8")

                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == key) {
                        return parser.getAttributeValue(null, "value")
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取 $key 失败", e)
        }
        return null
    }

    // 写入值到 XML
    private fun writeValue(key: String, value: String) {
        val allSettings = getAllSettings().toMutableMap()
        allSettings[key] = value
        writeAllSettings(allSettings)
    }

    // 写入所有设置到 XML
    private fun writeAllSettings(settings: Map<String, String>) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val serializer = factory.newSerializer()
            val writer = StringWriter()

            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)

            // 根元素
            serializer.startTag(null, "settings")

            // 写入所有设置
            settings.forEach { (key, value) ->
                serializer.startTag(null, key)
                serializer.attribute(null, "value", value)
                serializer.endTag(null, key)
            }

            serializer.endTag(null, "settings")
            serializer.endDocument()

            // 写入文件
            settingsFile.writeText(writer.toString())

        } catch (e: Exception) {
            Log.e(TAG, "写入设置失败", e)
        }
    }

    // 创建默认设置文件
    private fun createDefaultSettings() {
        val defaultSettings = mapOf(
            Keys.FIXED_MENU to "false"
        )
        writeAllSettings(defaultSettings)
        Log.d(TAG, "已创建默认设置文件")
    }
}