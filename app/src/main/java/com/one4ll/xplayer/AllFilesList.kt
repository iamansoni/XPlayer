package com.one4ll.xplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.one4ll.xplayer.adapter.VideoRecyclerViewAdapter
import com.one4ll.xplayer.database.MediaDatabase
import com.one4ll.xplayer.helpers.*
import com.one4ll.xplayer.models.Video
import kotlinx.android.synthetic.main.activity_all_files_list.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val READ_AND_WRITE_STORAGE_PERMISSION = 3
private const val TAG = "MainActivity"

class AllFilesList : AppCompatActivity() {
    private var thumbnail = File("")
    private lateinit var videoRecyclerViewAdapter: VideoRecyclerViewAdapter
    private lateinit var mediaDatabase: MediaDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_files_list)
        thumbnail.delete()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val videoList = ArrayList<Media>()
        videoRecyclerViewAdapter = VideoRecyclerViewAdapter(this, videoList)

        video_list_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        video_list_recycler_view.adapter = videoRecyclerViewAdapter
        mediaDatabase = MediaDatabase.getInstance(this)
        if (readAndWriteExternalStoragePermission()) {
            getVideoList()
        }
    }

    private fun getVideoList() {
        val externalVideoList = getExternalContentVideoUri(this)
        val internalVideoList = getInternalContentVideoUri(this)
        externalVideoList.addAll(internalVideoList)
        videoRecyclerViewAdapter.loadVideo(externalVideoList)
        lifecycleScope.launch {
            withContext(IO) {
                var count = 0
                //todo fix  - remove delete all and fix auto increment id
                mediaDatabase.videoDao().deleteAll()
                externalVideoList.forEach {
                    val medium = Video(count++, it.name, it.path, it.duration)
                    mediaDatabase.videoDao().insert(medium)
                }
                Log.d(TAG, "after update data base")
                mediaDatabase.videoDao().getAll().forEach {
                    Log.d(TAG, "form media data base $it")

                }
            }
        }
    }


    private fun readAndWriteExternalStoragePermission(): Boolean {
        if (IS_MARSHMALLOW_OR_LETTER()) {
            if (!havePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                askPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionId = READ_AND_WRITE_STORAGE_PERMISSION)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_AND_WRITE_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getVideoList()
                }
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        mediaDatabase.close()
        MediaDatabase.destroyInstance()
    }

}