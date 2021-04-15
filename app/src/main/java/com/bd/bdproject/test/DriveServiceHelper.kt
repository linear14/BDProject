package com.bd.bdproject.test

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
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
}