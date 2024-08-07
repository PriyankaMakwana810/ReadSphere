package com.tridya.readsphere.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.tridya.readsphere.BuildConfig
import com.tridya.readsphere.R
import com.tridya.readsphere.base.BaseFragment
import com.tridya.readsphere.databinding.FragmentSettingsBinding
import com.tridya.readsphere.utils.gone

class SettingsFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.fragmentHome)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )


        binding.toolBar.ivFavorites.gone()
        binding.toolBar.tvTitle.text = requireContext().getString(R.string.settings)

        binding.tvVersion.text = "v${BuildConfig.VERSION_NAME}"
        binding.ivAppIcon.setImageResource(R.mipmap.ic_launcher)

    }

}