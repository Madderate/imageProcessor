package com.devmcry.imageprocessor.ui.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.devmcry.imageprocessor.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditPicFragment : Fragment() {
    companion object {
        private const val REQ_CHOOSE_PICS = 100
    }

    private val editPicViewModel: EditPicViewModel by viewModels()

    private lateinit var choosePics: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit, container, false)
        choosePics = root.findViewById(R.id.choosePics)
        choosePics.setOnClickListener {
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
        editPicViewModel.imagePath.observe(viewLifecycleOwner, {
        })
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CHOOSE_PICS -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            data.clipData?.run {
                                val bitmapPaths = mutableListOf<String>()
                                for (i in 0 until itemCount) {
                                    val uri = getItemAt(i).uri
                                    bitmapPaths.add(uri.toString())
                                }
                                withContext(Dispatchers.Main){
                                    editPicViewModel.setEditImages(bitmapPaths)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}