/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.adapters.VoiceListAdapter

class VoiceSelectorDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(name: String) : VoiceSelectorDialogFragment {
            val voiceSelectorDialogFragment = VoiceSelectorDialogFragment()

            val args = Bundle()
            args.putString("name", name)

            voiceSelectorDialogFragment.arguments = args

            return voiceSelectorDialogFragment
        }
    }


    private var builder: AlertDialog.Builder? = null

    private var listener: OnVoiceSelectedListener? = null

    private var voiceList: ListView? = null

    private var voiceListAdapter: VoiceListAdapter? = null

    private var availableVoices: ArrayList<String> = arrayListOf()

    private var tts: android.speech.tts.TextToSpeech? = null

    private var voiceSelectedListener: VoiceListAdapter.OnItemClickListener =
        VoiceListAdapter.OnItemClickListener { model ->
            run {
                val preferences = Preferences.getPreferences(requireActivity(), "")
                preferences.setVoice(model)
                voiceListAdapter?.notifyDataSetChanged()
                listener?.onVoiceSelected(model)
                dismiss()
            }
        }

    private val ttsListener: android.speech.tts.TextToSpeech.OnInitListener =
        android.speech.tts.TextToSpeech.OnInitListener { _ ->
            var lng = Preferences.getPreferences(requireActivity(), "").getLanguage()

            if (lng.lowercase().contains("cn")) {
                lng = "cn"
            }

            if (lng.lowercase().contains("zh")) {
                lng = "zh"
            }

            for (voice in tts!!.voices) {
                if (voice.name.lowercase().contains("${lng.lowercase()}-")) {
                    availableVoices.add(voice.name)
                }
            }

            availableVoices.sort()

            voiceListAdapter = VoiceListAdapter(this.requireContext(), availableVoices)
            voiceListAdapter?.setOnItemClickListener(voiceSelectedListener)
            voiceList?.divider = null
            voiceList?.adapter = voiceListAdapter
            voiceListAdapter?.notifyDataSetChanged()
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_select_voice, null)

        voiceList = view.findViewById(R.id.voices_list)

        tts = android.speech.tts.TextToSpeech(requireContext(), ttsListener, "com.google.android.tts")

        builder!!.setView(view)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel, null)

        return builder!!.create()
    }

    fun interface OnVoiceSelectedListener {
        fun onVoiceSelected(voice: String)
    }

    fun setVoiceSelectedListener(listener: OnVoiceSelectedListener) {
        this.listener = listener
    }
}
