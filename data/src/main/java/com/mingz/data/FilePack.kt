package com.mingz.data

import android.content.Context
import com.mingz.share.quoteFile
import java.io.File

/**
 * 用于延迟初始化存储在files目录下的文件.
 * @see quoteFile
 */
class FilePack(private val childPath: String) {
    lateinit var file: File
        private set

    /**
     * 初始化文件.
     *
     * 默认存储在外部存储.
     * @see quoteFile
     */
    fun init(context: Context, atInternal: Boolean = false) {
        file = quoteFile(context, atInternal, childPath)
    }
}