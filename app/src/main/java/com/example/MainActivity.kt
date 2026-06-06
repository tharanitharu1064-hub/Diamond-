package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diamond.ColorLegendItem
import com.example.diamond.DiamondArtKit
import com.example.diamond.DiamondGridViewer
import com.example.diamond.DiamondPatternViewModel
import com.example.diamond.SampleImages
import com.example.diamond.ViewMode
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) { innerPadding ->
                    DiamondCreatorDashboard(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DiamondCreatorDashboard(
    modifier: Modifier = Modifier,
    viewModel: DiamondPatternViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // State collection
    val sourceBitmap by viewModel.sourceBitmap.collectAsState()
    val activeKit by viewModel.activeKit.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val targetCellLimit by viewModel.targetCellLimit.collectAsState()
    val paletteSize by viewModel.paletteSize.collectAsState()
    val grayscaleOnly by viewModel.grayscaleOnly.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val highlightedItem by viewModel.highlightedItem.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()

    // File selection launcher
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadCustomUri(context, uri)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isDesktop = maxWidth > 760.dp

        if (isDesktop) {
            // Desktop Two-Pane layout
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left Panel - Controls & Information (Fixed Width)
                Column(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    LogoHeader()
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ImageInputControls(
                        currentPreset = currentPreset,
                        onPresetSelected = { viewModel.loadPreset(it) },
                        onUploadClick = { imageLauncher.launch("image/*") }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsControls(
                        targetCellLimit = targetCellLimit,
                        paletteSize = paletteSize,
                        grayscaleOnly = grayscaleOnly,
                        onLimitChanged = { viewModel.updateGridResolution(it) },
                        onPaletteSizeChanged = { viewModel.updateColorPaletteSize(it) },
                        onGrayscaleToggled = { viewModel.toggleGrayscale(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ShoppingListSection(
                        kit = activeKit,
                        highlightedItem = highlightedItem,
                        onItemClick = { viewModel.toggleHighlightLegend(it) },
                        onClearHighlight = { viewModel.clearHighlight() }
                    )
                }

                // Right Panel - Interactive Live Canvas Screen
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    CanvasTopBar(
                        viewMode = viewMode,
                        onViewModeChanged = { viewModel.setViewMode(it) },
                        activeKit = activeKit,
                        highlightedItem = highlightedItem,
                        onClearHighlight = { viewModel.clearHighlight() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(32.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val kit = activeKit
                            if (kit != null) {
                                DiamondGridViewer(
                                    kit = kit,
                                    viewMode = viewMode,
                                    highlightedColorLabel = highlightedItem?.label,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("diamond_grid_canvas")
                                )
                                
                                // Beautiful top-right absolutes overlay badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .shadow(3.dp, RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%,d", kit.totalDiamonds)} STONES",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            } else {
                                Text(
                                    "No pattern active. Please choose a preset or upload an image.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isProcessing) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.45f))
                                ) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .testTag("processing_indicator"),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Pixelating Grid & Clustering Colors...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InstructionGuideCard()
                }
            }
        } else {
            // Mobile Stacked UI Layout with Tabs
            var selectedMobileTab by remember { mutableIntStateOf(0) }
            val tabTitles = listOf("Design", "Setup", "Shopping List")

            Column(modifier = Modifier.fillMaxSize()) {
                LogoHeader(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                TabRow(
                    selectedTabIndex = selectedMobileTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedMobileTab == index,
                            onClick = { selectedMobileTab = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedMobileTab) {
                        0 -> { // Design Canvas Viewer
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                CanvasTopBar(
                                    viewMode = viewMode,
                                    onViewModeChanged = { viewModel.setViewMode(it) },
                                    activeKit = activeKit,
                                    highlightedItem = highlightedItem,
                                    onClearHighlight = { viewModel.clearHighlight() }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .shadow(6.dp, RoundedCornerShape(32.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(32.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val kit = activeKit
                                        if (kit != null) {
                                            DiamondGridViewer(
                                                kit = kit,
                                                viewMode = viewMode,
                                                highlightedColorLabel = highlightedItem?.label,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .testTag("diamond_grid_canvas_mobile")
                                            )
                                            
                                            // Beautiful top-right absolute overlay badge
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(10.dp)
                                                    .shadow(2.dp, RoundedCornerShape(20.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${String.format("%,d", kit.totalDiamonds)} STONES",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }

                                        if (isProcessing) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.55f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(12.dp)
                                                ) {
                                                    LinearProgressIndicator(
                                                        modifier = Modifier.width(60.dp),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Processing...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                InstructionGuideCard(isCompact = true)
                            }
                        }
                        1 -> { // Configuration Settings & Custom Uploads
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                ImageInputControls(
                                    currentPreset = currentPreset,
                                    onPresetSelected = { viewModel.loadPreset(it) },
                                    onUploadClick = { imageLauncher.launch("image/*") }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingsControls(
                                    targetCellLimit = targetCellLimit,
                                    paletteSize = paletteSize,
                                    grayscaleOnly = grayscaleOnly,
                                    onLimitChanged = { viewModel.updateGridResolution(it) },
                                    onPaletteSizeChanged = { viewModel.updateColorPaletteSize(it) },
                                    onGrayscaleToggled = { viewModel.toggleGrayscale(it) }
                                )
                            }
                        }
                        2 -> { // Shopping List & Color Palette Table
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                ShoppingListSection(
                                    kit = activeKit,
                                    highlightedItem = highlightedItem,
                                    onItemClick = { viewModel.toggleHighlightLegend(it) },
                                    onClearHighlight = { viewModel.clearHighlight() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val gemColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .background(gemColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "G",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "GemCanvas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp,
                lineHeight = 20.sp
            )
            Text(
                text = "DIAMOND KIT PRO",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageInputControls(
    currentPreset: SampleImages.SampleType,
    onPresetSelected: (SampleImages.SampleType) -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Image Palette Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Select one of our templates or upload any portrait/landscape photo to convert into diamond painting pixel art.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Upload custom button
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("upload_image_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload Photo",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Photo (PC/Laptop)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Instant Templates:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // FlowRow wraps presets nicely on different viewports
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SampleImages.SampleType.values().forEach { type ->
                    val isSelected = currentPreset == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPresetSelected(type) },
                        label = { Text(type.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsControls(
    targetCellLimit: Int,
    paletteSize: Int,
    grayscaleOnly: Boolean,
    onLimitChanged: (Int) -> Unit,
    onPaletteSizeChanged: (Int) -> Unit,
    onGrayscaleToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. Customize Painting Kit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Grayscale shading option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Grayscale Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Use only black, grey, & white tones.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = grayscaleOnly,
                    onCheckedChange = onGrayscaleToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("grayscale_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Resolution slider (number of pixel cubes)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Grid Resolution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$targetCellLimit Stones",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Controls pixel size (split up to 32,000 blocks). Low values are easier for beginners.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Slider mapping to standard density marks
                val stepValues = listOf(400, 1600, 3600, 6400, 14400, 25600, 32000)
                val currentIdx = stepValues.indexOf(targetCellLimit).coerceAtLeast(0)
                
                Slider(
                    value = currentIdx.toFloat(),
                    onValueChange = { index ->
                        val roundedIdx = index.toInt().coerceIn(0, stepValues.size - 1)
                        onLimitChanged(stepValues[roundedIdx])
                    },
                    valueRange = 0f..(stepValues.size - 1).toFloat(),
                    steps = stepValues.size - 2,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(28.dp).testTag("resolution_slider")
                )
                
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    Text("400 (Coarse)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("6.4K (Detail)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("32K (Extreme)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Palette colors slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Distinct Colors",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$paletteSize Shades",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Defines how many separate letters/bags needed.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = paletteSize.toFloat(),
                    onValueChange = { onPaletteSizeChanged(it.toInt()) },
                    valueRange = 4f..32f,
                    steps = 27,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(28.dp).testTag("palette_slider")
                )
                
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    Text("4 Colors", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("16 Colors", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("32 Colors", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun CanvasTopBar(
    viewMode: ViewMode,
    onViewModeChanged: (ViewMode) -> Unit,
    activeKit: DiamondArtKit?,
    highlightedItem: ColorLegendItem?,
    onClearHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // View Mode Selectors
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View Layout: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ViewMode.values().forEach { mode ->
                        val isSelected = viewMode == mode
                        val btnColor = if (isSelected) {
                            ButtonDefaults.filledTonalButtonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        }
                        
                        OutlinedButton(
                            onClick = { onViewModeChanged(mode) },
                            shape = RoundedCornerShape(6.dp),
                            colors = btnColor,
                            modifier = Modifier.height(28.dp).testTag("view_mode_${mode.name.lowercase()}"),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text(mode.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Grid Stats display
                if (activeKit != null) {
                    Text(
                        text = "Grid: ${activeKit.gridWidth} x ${activeKit.gridHeight} px",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Highlighting Active Alert banner
            if (highlightedItem != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(highlightedItem.colorValue))
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Focus Mode On: Row [${highlightedItem.label}] is highlighted! Other colors dimmed.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(
                        onClick = onClearHighlight,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Highlight",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListSection(
    kit: DiamondArtKit?,
    highlightedItem: ColorLegendItem?,
    onItemClick: (ColorLegendItem) -> Unit,
    onClearHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Helper mapper to provide stunning authentic DMC naming
    fun getDmcColorName(dmcCode: String): String {
        return when (dmcCode) {
            "310" -> "Jet Black"
            "413" -> "Graphite Gray"
            "318" -> "Mist Grey"
            "743" -> "Cadmium Yellow"
            "666" -> "Bright Red"
            "995" -> "Electric Blue"
            "702" -> "Kelly Green"
            "606" -> "Vibrant Orange"
            "3371" -> "Bitter Black Brown"
            "154" -> "Very Dark Grape"
            "796" -> "Royal Blue"
            "907" -> "Light Parrot Green"
            "3837" -> "Ultra Violet"
            else -> "Floss Shade #$dmcCode"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Title and total colors pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shopping List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (kit != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${kit.legend.size} Colors Total",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (kit == null) {
                Text(
                    text = "No pattern active. Select a design above.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Stones: ${kit.totalDiamonds}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (highlightedItem != null) {
                        Text(
                            text = "Reset Focus",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onClearHighlight() }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable list items
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    kit.legend.forEach { item ->
                        val isSelected = highlightedItem?.label == item.label
                        
                        val bgStateColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )

                        val dmcName = getDmcColorName(item.dmcEstimate)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(if (isSelected) 2.dp else 0.dp, RoundedCornerShape(16.dp))
                                .background(bgStateColor, RoundedCornerShape(16.dp))
                                .border(
                                    1.dp, 
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onItemClick(item) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Letter block icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(2.dp, RoundedCornerShape(12.dp))
                                    .background(Color(item.colorValue), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Contrast text color calculation based on background luminance
                                val r = Color(item.colorValue).red
                                val g = Color(item.colorValue).green
                                val b = Color(item.colorValue).blue
                                val luminance = 0.299 * r + 0.587 * g + 0.114 * b
                                val letterColor = if (luminance > 0.5) Color.Black else Color.White

                                Text(
                                    text = item.label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = letterColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))

                            // Color title & code
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dmcName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "DMC Code: ${item.dmcEstimate}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Numerical totals and tag info
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("%,d", item.count),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primaryOnBg()
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "GEMS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Color helper to avoid naming clashes or duplicate imports
@Composable
private fun ColorScheme.primaryOnBg(): Color {
    return if (isSystemInDarkTheme()) this.primary else Color(0xFF1E1B4B) // Very dark indigo
}

@Composable
fun InstructionGuideCard(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Guideline Help",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Desktop Workspace Gestures / Tutorial:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!isCompact) {
                    Text(
                        text = "• PINCH or SCROLL TRACKPAD to zoom in/out (from coarse overview down to pixel blocks).\n" +
                               "• DRAG with mouse or trackpad to pan around zoomed coordinate quadrants.\n" +
                               "• TAP any color row on the shopping list to instantly light up matching diamonds on the grid.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 13.sp
                    )
                } else {
                    Text(
                        text = "Pinch to zoom & drag to pan canvas. Tap color rows in shopping list to focus.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Support extension for Surface Background coloring
val ColorScheme.surfaceCard: Color
    @Composable
    get() = if (isSystemInDarkTheme()) {
        Color(0xFF282830)
    } else {
        Color(0xFFFBFBFC)
    }
