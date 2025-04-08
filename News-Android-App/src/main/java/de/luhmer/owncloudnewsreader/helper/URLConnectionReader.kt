@file:JvmName("URLConnectionReader")

package de.luhmer.owncloudnewsreader.helper

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

/**
 * Created by David on 13.01.2016.
 */
@Throws(IOException::class)
fun getText(url: String?): String {
    val website = URL(url)
    val connection = website.openConnection()

    val response = StringBuilder()
    BufferedReader(InputStreamReader(connection.getInputStream())).use { inReader ->
        {
            var inputLine: String?
            while (inReader.readLine().also { inputLine = it } != null) response.append(inputLine)
        }
    }
    return response.toString()
}
