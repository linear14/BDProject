package com.bd.bdproject.ui.collection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bd.bdproject.R
import com.bd.bdproject.databinding.ActivityCollectionMainBinding

class CollectionMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollectionMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCollectionMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }



    }
}