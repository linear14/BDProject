package com.bd.bdproject.view.activity

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.observe
import com.bd.bdproject.data.model.Tags
import com.bd.bdproject.databinding.ActivityDetailBinding
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.util.Constant.CONTROL_MEMO
import com.bd.bdproject.util.Constant.CONTROL_TAG
import com.bd.bdproject.util.Constant.DETAIL
import com.bd.bdproject.util.Constant.INFO_DATE_CODE
import com.bd.bdproject.util.Constant.INFO_DESTINATION
import com.bd.bdproject.util.Constant.INFO_LIGHT
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.util.Constant.INFO_SHOULD_HAVE_DRAWER
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.util.toBitDamDateFormat
import com.bd.bdproject.view.adapter.TagAdapter
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import org.koin.android.ext.android.inject

class DetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailBinding

    private val lightViewModel: LightViewModel by inject()
    private var tagAdapter: TagAdapter? = null

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
    }

    override fun onResume() {
        super.onResume()

        lightViewModel.getLightWithTags(intent.getStringExtra(INFO_DATE_CODE)?:System.currentTimeMillis().timeToString())
        initButtons()
        observeLight()
        setTagRecyclerView()
        setEditButtons()
    }

    private fun initButtons() {
        binding.apply {
            when(intent.getBooleanExtra(INFO_SHOULD_HAVE_DRAWER, true)) {
                true -> {
                    btnDrawer.visibility = View.VISIBLE
                    btnBack.visibility = View.GONE
                }
                false -> {
                    btnDrawer.visibility = View.GONE
                    btnBack.visibility = View.VISIBLE
                }
            }
            fabMore.setOnClickListener { controlBackgroundByFabState() }
            viewFilter.setOnClickListener { controlBackgroundByFabState() }
        }
    }

    private fun observeLight() {
        lightViewModel.lightWithTags.observe(owner = this) {
            binding.apply {
                val dateCode = it.light.dateCode
                val brightness = it.light.bright
                val memo = it.light.memo
                val tags = it.tags

                setColorAllViews(brightness)

                tvDate.text = dateCode.toBitDamDateFormat()
                tvBrightness.text = brightness.toString()
                tvMemo.text = memo?:""
                gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
                layoutLightDetail.background = gradientDrawable

                tagAdapter?.submitList(tags.toMutableList(), brightness)
            }
        }
    }

    private fun setTagRecyclerView() {
        binding.apply {
            val layoutManager = FlexboxLayoutManager(this@DetailActivity).apply {
                flexDirection = FlexDirection.COLUMN
            }
            tagAdapter = TagAdapter()

            rvTag.layoutManager = layoutManager
            rvTag.adapter = tagAdapter
        }
    }

    private fun controlBackgroundByFabState() {
        binding.apply {
            when(viewFilter.visibility) {
                View.GONE -> {
                    viewFilter.visibility = View.VISIBLE
                    layoutMore.visibility = View.VISIBLE
                    binding.apply {
                        btnDrawer.visibility = View.GONE
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }

                }
                View.VISIBLE -> {
                    viewFilter.visibility = View.GONE
                    layoutMore.visibility = View.GONE
                    binding.apply {
                        btnDrawer.visibility = View.VISIBLE
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                }
            }
        }
    }

    private fun setColorAllViews(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                tvDate,
                tvMemo,
                btnSpreadUpDown,
                binding.btnDrawer,
                binding.btnBack
            )
        }
    }

    private fun setEditButtons() {
        binding.apply {
            val intent = Intent(this@DetailActivity, BitdamEditActivity::class.java).apply {
                putExtra(INFO_PREVIOUS_ACTIVITY, DETAIL)
            }

            actionEditBrightness.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                startActivity(intent.apply {
                    putExtra(INFO_DESTINATION, CONTROL_BRIGHTNESS)
                    putExtra(INFO_LIGHT, light)
                })
            }

            actionEditTag.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                val tags = Tags()
                for(i in lightViewModel.lightWithTags.value?.tags?: mutableListOf()) {
                    tags.add(i)
                }
                startActivity(intent.apply {
                    putExtra(INFO_DESTINATION, CONTROL_TAG)
                    putExtra(INFO_LIGHT, light)
                    putExtra(INFO_TAG, tags as Parcelable)
                })
            }

            actionEditMemo.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                startActivity(intent.apply {
                    putExtra(INFO_DESTINATION, CONTROL_MEMO)
                    putExtra(INFO_LIGHT, light)
                })
            }
        }
    }
}