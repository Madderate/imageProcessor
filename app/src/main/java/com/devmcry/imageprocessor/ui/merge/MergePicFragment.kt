package com.devmcry.imageprocessor.ui.merge

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.devmcry.imageprocessor.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MergePicFragment : Fragment() {
    companion object {
        private const val REQ_CHOOSE_PICS = 100
    }

    private val homeViewModel: MergePicViewModel by viewModels()

    private lateinit var mergeResult: TextView
    private lateinit var choosePics: Button
    private lateinit var mergeResultImage: SubsamplingScaleImageView

    private val dialog by lazy { ProgressDialog(context) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        mergeResult = root.findViewById(R.id.mergeResult)
        mergeResultImage = root.findViewById(R.id.mergeResultImage)
        homeViewModel.text.observe(viewLifecycleOwner, {
            mergeResult.text = it
        })
        homeViewModel.bitmap.observe(viewLifecycleOwner, {
            dialog.hide()
            if (it != null) {
                mergeResultImage.setImage(ImageSource.bitmap(it))
            }
        })
        choosePics = root.findViewById(R.id.choosePics)
        choosePics.setOnClickListener {
            homeViewModel.clear()
            Intent().run {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(this, getString(R.string.choose_pics)),
                    REQ_CHOOSE_PICS
                )
            }
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CHOOSE_PICS -> {
                if (resultCode == RESULT_OK && data != null) {
                    dialog.show()
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            data.clipData?.run {
                                val bitmaps = mutableListOf<Bitmap>()
                                val bitmapPaths = mutableListOf<String>()
                                for (i in 0 until itemCount) {
                                    val uri = getItemAt(i).uri
                                    withContext(Dispatchers.Main) {
                                        homeViewModel.append(uri.toString())
                                    }
                                    val bitmap = MediaStore.Images.Media.getBitmap(
                                        context?.contentResolver,
                                        uri
                                    )
                                    bitmaps.add(bitmap)
                                    bitmapPaths.add(uri.toString())
                                }
                                context?.run {
                                    homeViewModel.mergeImage(this, bitmapPaths)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}