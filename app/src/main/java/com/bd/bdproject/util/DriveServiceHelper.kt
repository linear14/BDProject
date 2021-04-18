package com.bd.bdproject.util

import com.bd.bdproject.BitdamLog
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

class DriveServiceHelper(val driveService: Drive) {

    val executor = Executors.newSingleThreadExecutor()

    fun createFile(filePath: String): Task<String> {
        return Tasks.call(executor) {

            val fileMetaData = File()
            fileMetaData.name = "빛담_${System.currentTimeMillis()}.db"

            val file = java.io.File(filePath)
            val mediaContent = FileContent("application/x-sqlite3", file)

            var myFile: File? = null
            try {
                myFile = driveService.files().create(fileMetaData, mediaContent).execute()
            } catch(e: Exception) {
                e.printStackTrace()
            }

            if(myFile == null) {
                throw IOException("Null result when requesting file creation")
            }

            myFile.id
        }
    }

    fun findFiles(): Task<Unit> {
        return Tasks.call(executor) {
            var pageToken: String? = null

            do {
                val result: FileList = driveService.files().list()
                    .setQ("mimeType='application/x-sqlite3'")
                    .setSpaces("drive")
                    .execute()

                for (file in result.files) {
                    BitdamLog.contentLogger("File Name: ${file.name}, File Id: ${file.id}")
                }
                pageToken = result.nextPageToken

            } while (pageToken != null)
        }
    }

    fun retrieveFile(fileId: String): Task<Unit> {
        return Tasks.call(executor) {
            try {
                val outputStream = FileOutputStream("/data/data/com.bd.bdproject/databases/BITDAM_DB")
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream)

                outputStream.flush()
                outputStream.close()
            } catch(e: Exception) {
                e.printStackTrace()
            }

        }
    }
}