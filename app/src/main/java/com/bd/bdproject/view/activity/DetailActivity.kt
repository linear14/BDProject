package com.bd.bdproject.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import com.bd.bdproject.R
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.ActivityDetailBinding
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.common.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.common.Constant.CONTROL_MEMO
import com.bd.bdproject.common.Constant.CONTROL_TAG
import com.bd.bdproject.common.Constant.DETAIL
import com.bd.bdproject.common.Constant.INFO_DATE_CODE
import com.bd.bdproject.common.Constant.INFO_DESTINATION
import com.bd.bdproject.common.Constant.INFO_LIGHT
import com.bd.bdproject.common.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.common.Constant.INFO_SHOULD_HAVE_DRAWER
import com.bd.bdproject.common.Constant.INFO_TAG
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.common.toBitDamDateFormat
import com.bd.bdproject.common.toFullDateToolbar
import com.bd.bdproject.view.adapter.TagAdapter
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailBinding

    private val lightViewModel: LightViewModel by inject()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by inject()
    private var tagAdapter: TagAdapter? = null

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    var isHideDetails = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lightViewModel.currentDateCode = intent.getStringExtra(INFO_DATE_CODE)?:System.currentTimeMillis().timeToString()

        binding = ActivityDetailBinding.inflate(layoutInflater).apply {
            setContentView(root)

            btnDrawer.setOnClickListener {
                drawer.openDrawer(GravityCompat.START)
            }

            navigationDrawer.root.setOnTouchListener { _, _ -> true }

            navigationDrawer.actionMyLight.setOnClickListener {
                startActivity(Intent(this@DetailActivity, CollectionMainActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
            }

            navigationDrawer.actionStatistic.setOnClickListener {
                startActivity(Intent(this@DetailActivity, StatisticActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
            }

            navigationDrawer.actionHash.setOnClickListener {
                startActivity(Intent(this@DetailActivity, ManageHashActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
            }

            navigationDrawer.actionSetting.setOnClickListener {
                checkEnrollStateViewModel.isVisitedSetting = true
                startActivity(Intent(this@DetailActivity, SettingActivity::class.java))
                drawer.closeDrawer(GravityCompat.START)
            }

            try {
                val pInfo: PackageInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName
                navigationDrawer.tvVersion.text = "ver. $version"
            } catch (e: Exception) {
                navigationDrawer.tvVersion.text = ""
            }

            btnBack.setOnClickListener { onBackPressed() }

            btnSpreadUpDown.setOnClickListener {
                showOrHideDetails(btnSpreadUpDown)
            }
        }

        observeLight()
    }

    override fun onResume() {
        super.onResume()

        if(checkEnrollStateViewModel.isVisitedSetting) {
            checkEnrollStateViewModel.isVisitedSetting = false
            CoroutineScope(Dispatchers.IO).launch {
                val deferred = checkEnrollStateViewModel.isEnrolledTodayAsync(System.currentTimeMillis().timeToString())
                val isEnrolledToday = deferred.await()

                // 오늘 등록되지 않은 빛일 경우 다음 페이지로 이동
                if (!isEnrolledToday) {
                    launch(Dispatchers.Main) {
                        val intent =
                            Intent(this@DetailActivity, BitdamEnrollActivity::class.java).apply {
                                putExtra(INFO_PREVIOUS_ACTIVITY, DETAIL)
                            }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    lightViewModel.getLightWithTags()

                    launch(Dispatchers.Main) {
                        initBackground()
                        setEditButtons()
                    }
                }
            }
        } else {
            lightViewModel.getLightWithTags()
            initBackground()
            setEditButtons()
        }

    }

    private fun initBackground() {
        binding.apply {
            when(intent.getBooleanExtra(INFO_SHOULD_HAVE_DRAWER, true)) {
                // 드로어가 필요한 경우 (오늘의 빛을 홈 화면에서 보는 경우)
                true -> {
                    btnDrawer.visibility = View.VISIBLE
                    btnBack.visibility = View.GONE
                    tvToolbarTitle.visibility = View.GONE
                    tvMent.visibility = View.VISIBLE
                    tvDate.visibility = View.VISIBLE
                    btnSpreadUpDown.visibility = View.VISIBLE
                }
                
                // 드로어가 필요없는 경우 (Collection Main에서 빛 상세보기 진입한 경우)
                false -> {
                    btnDrawer.visibility = View.GONE
                    btnBack.visibility = View.VISIBLE
                    tvToolbarTitle.visibility = View.VISIBLE
                    tvMent.visibility = View.GONE
                    tvDate.visibility = View.GONE
                    btnSpreadUpDown.visibility = View.GONE
                }
            }
            fabMore.setOnClickListener { controlBackgroundByFabState() }
            viewFilter.setOnClickListener { controlBackgroundByFabState() }
        }
        setTagRecyclerView()
    }

    private fun observeLight() {
        lightViewModel.lightWithTags.observe(this) {
            binding.apply {
                val dateCode = it.light.dateCode
                val brightness = it.light.bright
                val memo = it.light.memo
                val tags = it.tags

                setColorAllViews(brightness)

                tvDate.text = dateCode.toBitDamDateFormat()
                tvToolbarTitle.text = dateCode.toFullDateToolbar()
                tvBrightness.text = brightness.toString()
                tvMemo.text = memo?:""
                gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
                layoutLightDetail.background = gradientDrawable

                // TODO 추천멘트 띄워주기

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
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }

                }
                View.VISIBLE -> {
                    viewFilter.visibility = View.GONE
                    layoutMore.visibility = View.GONE
                    binding.apply {
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
                tvToolbarTitle,
                tvMent,
                tvBrightness,
                tvDate,
                tvMemo,
                btnSpreadUpDown,
                btnDrawer,
                btnBack
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
                startForResult.launch(intent.apply {
                    putExtra(INFO_DESTINATION, CONTROL_BRIGHTNESS)
                    putExtra(INFO_LIGHT, light)
                })
                controlBackgroundByFabState()
            }

            actionEditTag.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                val tags = ArrayList<Tag>()
                for(i in lightViewModel.lightWithTags.value?.tags?: mutableListOf()) {
                    tags.add(i)
                }
                startForResult.launch(intent.apply {
                    putExtra(INFO_DESTINATION, CONTROL_TAG)
                    putExtra(INFO_LIGHT, light)
                    putParcelableArrayListExtra(INFO_TAG, tags)
                })
                controlBackgroundByFabState()
            }

            actionEditMemo.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                startForResult.launch(intent.apply {
                    putExtra(INFO_DESTINATION, CONTROL_MEMO)
                    putExtra(INFO_LIGHT, light)
                })
                controlBackgroundByFabState()
            }
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            Log.d("INTENT_TEST", "${intent?.getStringExtra("INTENT_TEST")}에서 돌아옴")
        }
    }

    private fun showOrHideDetails(arrow: ImageView) {
        val arrowRotation: Animation

        if(isHideDetails) {
            arrowRotation = AnimationUtils.loadAnimation(this, R.anim.animation_rotation_clockwise_half).apply{ fillAfter = true }
            binding.apply {
                layoutDetail.visibility = View.VISIBLE
                layoutDetail.startAnimation(AnimationUtils.loadAnimation(this.root.context, R.anim.slide_down))
            }
        } else {
            arrowRotation = AnimationUtils.loadAnimation(this, R.anim.animation_rotation_anticlockwise_half).apply{ fillAfter = true }
            binding.apply {
                layoutDetail.visibility = View.GONE
                layoutDetail.startAnimation(AnimationUtils.loadAnimation(this.root.context, R.anim.slide_up))
            }
        }
        arrow.startAnimation(arrowRotation)
        isHideDetails = !isHideDetails
    }

    override fun onBackPressed() {
        binding.apply {
            if(viewFilter.visibility == View.VISIBLE) {
                viewFilter.visibility = View.GONE
                layoutMore.visibility = View.GONE
                binding.apply {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            } else {
                super.onBackPressed()
            }
        }

    }
}