package com.xihh.base.android

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


class VBindViewHolder<VB: ViewBinding>(val binding: VB): RecyclerView.ViewHolder(binding.root)