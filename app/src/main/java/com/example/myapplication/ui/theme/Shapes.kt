package com.example.myapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Extra small components (chips, tags)
    extraSmall = RoundedCornerShape(8.dp),
    
    // Small components (buttons, text fields)
    small = RoundedCornerShape(12.dp),
    
    // Medium components (cards, dialogs)
    medium = RoundedCornerShape(16.dp),
    
    // Large components (sheets, large cards)
    large = RoundedCornerShape(20.dp),
    
    // Extra large components (full screen)
    extraLarge = RoundedCornerShape(28.dp)
)
