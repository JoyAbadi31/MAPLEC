package com.example.mapproject.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mapproject.databinding.DialogVendorBindBinding // This is auto-generated

class VendorBindDialog : DialogFragment() {

    // Binding variable
    private var _binding: DialogVendorBindBinding? = null
    private val binding get() = _binding!! // Accessor for the binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using ViewBinding
        _binding = DialogVendorBindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle Submit button click
        binding.submitButton.setOnClickListener {
            val vendorKey = binding.vendorBindEditText.text.toString()
            if (vendorKey.isNotEmpty()) {
                // Logic to handle the vendor key (for example, submitting it to Firebase)
                Toast.makeText(requireContext(), "Vendor Key Submitted: $vendorKey", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialogs
            } else {
                Toast.makeText(requireContext(), "Please enter a vendor key.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding when the view is destroyed to avoid memory leaks
        _binding = null
    }
}
