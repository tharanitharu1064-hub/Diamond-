package com.example.diamond

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

enum class ViewMode(val label: String, val description: String) {
    COLOR_WITH_LETTER("Color + Letter", "Standard view showing colored diamond squares with high-contrast letters."),
    LETTER_ONLY("Letter Only", "Symbol canvas layout. Plain high-contrast empty cells with alphabetic markers."),
    COLOR_ONLY("Art Render", "Pixel painting presentation. Clean colored tiles without character legends.")
}

class DiamondPatternViewModel : ViewModel() {

    // Preset / Sample selection
    private val _currentPreset = MutableStateFlow(SampleImages.SampleType.SUNFLOWER)
    val currentPreset: StateFlow<SampleImages.SampleType> = _currentPreset.asStateFlow()

    // Custom URI image loaded (if any)
    private val _customImageUri = MutableStateFlow<Uri?>(null)
    val customImageUri: StateFlow<Uri?> = _customImageUri.asStateFlow()

    // Bitmap in use (Preset or Loaded Uri)
    private val _sourceBitmap = MutableStateFlow<Bitmap?>(null)
    val sourceBitmap: StateFlow<Bitmap?> = _sourceBitmap.asStateFlow()

    // Grid limitation size (up to 32,000 blocks)
    private val _targetCellLimit = MutableStateFlow(1600) // Default 40x40 = 1600 elements
    val targetCellLimit: StateFlow<Int> = _targetCellLimit.asStateFlow()

    // Number of distinct colors (palette)
    private val _paletteSize = MutableStateFlow(12) // Default 12 colors
    val paletteSize: StateFlow<Int> = _paletteSize.asStateFlow()

    // Grayscale shader filter active
    private val _grayscaleOnly = MutableStateFlow(false)
    val grayscaleOnly: StateFlow<Boolean> = _grayscaleOnly.asStateFlow()

    // Render Mode toggle
    private val _viewMode = MutableStateFlow(ViewMode.COLOR_WITH_LETTER)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Highlighted Color Indicator/Legend item
    private val _highlightedItem = MutableStateFlow<ColorLegendItem?>(null)
    val highlightedItem: StateFlow<ColorLegendItem?> = _highlightedItem.asStateFlow()

    // Is processing in background?
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Computed Art Kit output
    private val _activeKit = MutableStateFlow<DiamondArtKit?>(null)
    val activeKit: StateFlow<DiamondArtKit?> = _activeKit.asStateFlow()

    init {
        // Initialize with default template
        loadPreset(SampleImages.SampleType.SUNFLOWER)

        // React dynamically to changes with a debounced flow
        observeParameters()
    }

    private fun observeParameters() {
        viewModelScope.launch {
            combine(
                _sourceBitmap,
                _targetCellLimit,
                _paletteSize,
                _grayscaleOnly
            ) { bitmap, count, colors, grays ->
                paramsTuple(bitmap, count, colors, grays)
            }
            .collect { tuple ->
                val bm = tuple.bitmap
                if (bm != null) {
                    recalculateKit(bm, tuple.limit, tuple.colors, tuple.grayscale)
                }
            }
        }
    }

    private data class paramsTuple(
        val bitmap: Bitmap?,
        val limit: Int,
        val colors: Int,
        val grayscale: Boolean
    )

    fun loadPreset(preset: SampleImages.SampleType) {
        _isProcessing.value = true
        _customImageUri.value = null
        _currentPreset.value = preset
        _highlightedItem.value = null

        viewModelScope.launch {
            val bm = withContext(Dispatchers.Default) {
                SampleImages.generate(preset, 180, 180)
            }
            _sourceBitmap.value = bm
        }
    }

    fun loadCustomUri(context: Context, uri: Uri) {
        _isProcessing.value = true
        _customImageUri.value = uri
        _highlightedItem.value = null

        viewModelScope.launch {
            val bm = withContext(Dispatchers.IO) {
                try {
                    val input: InputStream? = context.contentResolver.openInputStream(uri)
                    val opts = BitmapFactory.Options().apply {
                        // Keep image size reasonable (scale down if huge initial photos are chosen)
                        inSampleSize = 2 
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeStream(input, null, opts)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (bm != null) {
                _sourceBitmap.value = bm
            } else {
                _isProcessing.value = false
            }
        }
    }

    fun updateGridResolution(limitValue: Int) {
        _targetCellLimit.value = limitValue
    }

    fun updateColorPaletteSize(sizeValue: Int) {
        _paletteSize.value = sizeValue
    }

    fun toggleGrayscale(enabled: Boolean) {
        _grayscaleOnly.value = enabled
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleHighlightLegend(item: ColorLegendItem) {
        if (_highlightedItem.value?.label == item.label) {
            _highlightedItem.value = null // clear highlight
        } else {
            _highlightedItem.value = item
        }
    }

    fun clearHighlight() {
        _highlightedItem.value = null
    }

    private fun recalculateKit(bitmap: Bitmap, cellLimit: Int, palette: Int, grayscale: Boolean) {
        _isProcessing.value = true
        viewModelScope.launch {
            val kit = withContext(Dispatchers.Default) {
                try {
                    PixelQuantizer.createDiamondKit(bitmap, cellLimit, palette, grayscale)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (kit != null) {
                _activeKit.value = kit
                // If a highlight was selected previously, update or clear it
                val previousHighlight = _highlightedItem.value
                if (previousHighlight != null) {
                    val updatedHighlight = kit.legend.find { it.label == previousHighlight.label }
                    _highlightedItem.value = updatedHighlight
                }
            }
            _isProcessing.value = false
        }
    }
}
