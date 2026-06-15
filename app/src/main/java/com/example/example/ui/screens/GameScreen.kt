package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog

@Composable
fun GameScreen(
    viewModel: LudoViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.gameEngine.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var isMuted by remember { mutableStateOf(!viewModel.soundManager.isSoundEnabled) }
    var showLiveRecordDialog by remember { mutableStateOf(false) }

    LudoSceneProvider {
    LudoAtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            
            // 1. GAMEPLAY HEADER WITH MUTE TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Double button cluster on left: Back & Exit
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LudoHeaderButton(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onNavigateBack,
                        contentDescription = "Back"
                    )
                    LudoHeaderButton(
                        imageVector = Icons.Default.ExitToApp,
                        onClick = onNavigateBack,
                        contentDescription = "Exit"
                    )
                }

                Text(
                    text = if (uiState.gameMode == GameMode.ETI_VS_RASEL) "RIVALRY SHOWDOWN" else "CLASSIC LUDO",
                    color = GoldLegend,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                // Double button cluster on right: Live Record & Mute
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LudoHeaderButton(
                        imageVector = Icons.Default.Assessment,
                        onClick = {
                            viewModel.soundManager.playClickSound()
                            showLiveRecordDialog = true
                        },
                        contentDescription = "Live Record"
                    )
                    LudoHeaderButton(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        onClick = {
                            isMuted = !isMuted
                            viewModel.soundManager.isSoundEnabled = !isMuted
                            viewModel.soundManager.isMusicEnabled = !isMuted
                            viewModel.soundManager.playClickSound()
                        },
                        contentDescription = "Mute"
                    )
                }
            }

            // 2. MAIN LAYOUT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                // Top Player Panels (Player 0 and Player 1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PlayerIndicatorPanel(
                        player = uiState.players.getOrNull(0),
                        isCurrentTurn = uiState.currentPlayerIndex == 0,
                        colorAccent = LudoRed,
                        diceValue = uiState.diceValue,
                        diceState = uiState.diceState,
                        onDiceRoll = { viewModel.gameEngine.rollDice() }
                    )
                    PlayerIndicatorPanel(
                        player = uiState.players.getOrNull(1),
                        isCurrentTurn = uiState.currentPlayerIndex == 1,
                        colorAccent = LudoYellow,
                        diceValue = uiState.diceValue,
                        diceState = uiState.diceState,
                        onDiceRoll = { viewModel.gameEngine.rollDice() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // The Premium responsive Ludo Board
                LudoBoardWidget(
                    uiState = uiState,
                    onTokenClick = { tokenId ->
                        viewModel.gameEngine.makeMove(tokenId)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Player Panels (Player 3 and Player 2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PlayerIndicatorPanel(
                        player = uiState.players.getOrNull(3),
                        isCurrentTurn = uiState.currentPlayerIndex == 3,
                        colorAccent = LudoGreen,
                        diceValue = uiState.diceValue,
                        diceState = uiState.diceState,
                        onDiceRoll = { viewModel.gameEngine.rollDice() }
                    )
                    PlayerIndicatorPanel(
                        player = uiState.players.getOrNull(2),
                        isCurrentTurn = uiState.currentPlayerIndex == 2,
                        colorAccent = LudoBlue,
                        diceValue = uiState.diceValue,
                        diceState = uiState.diceState,
                        onDiceRoll = { viewModel.gameEngine.rollDice() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. DRAMATIC ETI VS RASEL BATTLE SCREEN INTRO OVERLAY
        AnimatedVisibility(
            visible = uiState.showVsIntro,
            enter = fadeIn() + scaleIn(initialScale = 1.3f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            EtiVsRaselBattleSplash(
                eti = uiState.players.firstOrNull { it.id == "eti" },
                rasel = uiState.players.firstOrNull { it.id == "rasel" }
            )
        }

        // 5. CELEBRATION WINNER OVERLAY
        AnimatedVisibility(
            visible = uiState.isGameOver,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut()
        ) {
            WinnerCelebrationOverlay(
                winner = uiState.winner,
                durationSec = uiState.matchDurationSec,
                onPlayAgain = {
                    viewModel.soundManager.playClickSound()
                    viewModel.startNewGame(
                        mode = uiState.gameMode,
                        numPlayers = uiState.players.filter { it.isActive }.size,
                        playAsEti = uiState.players.firstOrNull { it.id == "eti" }?.isHuman == true,
                        isLocalPvP = uiState.players.filter { it.isActive && it.colorIndex != 0 }.any { it.isHuman }
                    )
                },
                onGoHome = {
                    viewModel.soundManager.playClickSound()
                    onNavigateBack()
                }
            )
        }

        // 6. LIVE RECORD STATISTICS INTERACTIVE BOARD OVERLAY
        if (showLiveRecordDialog) {
            LiveRecordDialog(
                uiState = uiState,
                onDismiss = { showLiveRecordDialog = false }
            )
        }
    }
    } // end LudoSceneProvider
}

// Indicator tag overlay for active turns
@Composable
fun PlayerIndicatorPanel(
    player: PlayerState?,
    isCurrentTurn: Boolean,
    colorAccent: Color,
    diceValue: Int,
    diceState: DiceState,
    onDiceRoll: () -> Unit
) {
    if (player == null || !player.isActive) {
        // Render dummy frame when player is inactive
        Box(
            modifier = Modifier
                .width(175.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(SlateCard.copy(alpha = 0.2f))
                .border(1.dp, SlateBorder.copy(alpha = 0.2f), RoundedCornerShape(34.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("NOT PLAYING", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        return
    }

    val glowTransition = rememberInfiniteTransition(label = "turn_pulse")
    val alphaColor by glowTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val cardBorderColor = if (isCurrentTurn) {
        colorAccent.copy(alpha = alphaColor)
    } else {
        SlateBorder.copy(alpha = 0.5f)
    }

    val isHandClickEnabled = isCurrentTurn && player.isHuman && diceState == DiceState.IDLE

    Row(
        modifier = Modifier
            .width(175.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(if (isCurrentTurn) colorAccent.copy(alpha = 0.15f) else SlateCard.copy(alpha = 0.7f))
            .border(
                width = if (isCurrentTurn) 2.5.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(34.dp)
            )
            .clickable(enabled = isHandClickEnabled, onClick = onDiceRoll)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendAvatarWithFrame(
            avatarUri = player.avatarUri,
            frameAsset = player.frameAsset,
            size = 48.dp
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Text(
                text = if (player.isHuman) "PLAYER" else "AI BOT",
                color = if (isCurrentTurn) colorAccent else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }

        if (isCurrentTurn) {
            Box(
                modifier = Modifier.padding(end = 4.dp)
            ) {
                MiniDiceAnimatorWidget(
                    diceValue = diceValue,
                    diceState = diceState,
                    isClickable = isHandClickEnabled,
                    colorAccent = colorAccent,
                    onClick = onDiceRoll,
                    size = 54.dp
                )
            }
        }
    }
}

// Beautiful Glossy/Neon Header circular button matching the screenshot
@Composable
fun LudoHeaderButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = ""
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF00838F))
                ),
                CircleShape
            )
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Computes the active steps percentage completion for all 4 tokens (0..224 steps max)
fun getPlayerCompletionPercentage(playerIndex: Int, tokens: List<TokenState>): String {
    val playerTokens = tokens.filter { it.playerIndex == playerIndex }
    if (playerTokens.isEmpty()) return "0.0%"
    val totalSteps = playerTokens.sumOf { it.step }
    val percentage = (totalSteps.toFloat() / 224f) * 100f
    return String.format(java.util.Locale.US, "%.1f%%", percentage)
}

// Gorgeous white title/percent overlays on bases
@Composable
fun LudoBoardTextOverlay(
    text: String,
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    cellSize: androidx.compose.ui.unit.Dp,
    color: Color = Color.White
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = cellSize * 6, height = cellSize * 0.85f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = (cellSize.value * 0.35f).sp,
            fontWeight = FontWeight.Bold,
            style = LocalTextStyle.current.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(1f, 1f),
                    blurRadius = 2.5f
                )
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// THE ENTIRE LUDO BOARD GRID GRAPHIC WIDGET
@Composable
fun LudoBoardWidget(
    uiState: LudoUIState,
    onTokenClick: (tokenId: Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val boardSize = minOf(maxWidth, maxHeight)
        val cellSize = boardSize / 15

        // Group tokens arriving at the exact same physical coordinates
        // TokenState Map: Key -> Pair(Row, Col), Value -> List of Tokens
        val tokensOnPositions = remember(uiState.tokens) {
            uiState.tokens.groupBy { t ->
                if (t.isFinished) {
                    LudoBoardMap.getGoalCoordinates(t.playerIndex)
                } else if (t.isYard) {
                    LudoBoardMap.getYardCoordinates(t.playerIndex, t.id)
                } else {
                    LudoBoardMap.getCellCoordinates(t.playerIndex, t.step)
                }
            }
        }

        Box(
            modifier = Modifier
                .size(boardSize)
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCard)
                .border(2.dp, SlateBorder, RoundedCornerShape(16.dp))
        ) {
            // 1. Draw static grid structures
            LudoBoardBackgroundDrawing(cellSize)

            // 2. Draw safe star markers (⭐) on star zones
            LudoBoardStarsDrawing(cellSize)

            // 3. Render Ludo Tokens
            tokensOnPositions.forEach { (coords, tokens) ->
                val (row, col) = coords
                val xOffset = cellSize * col
                val yOffset = cellSize * row

                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .size(cellSize),
                    contentAlignment = Alignment.Center
                ) {
                    // Cluster layout depending on number of tokens occupying the same grid tile
                    val count = tokens.size
                    if (count == 1) {
                        val token = tokens[0]
                        LudoTokenPiece(
                            token = token,
                            isClickable = uiState.validMoves.contains(token.id) && uiState.currentPlayerIndex == token.playerIndex && !uiState.isGameOver,
                            onClick = { onTokenClick(token.id) },
                            isAnimating = uiState.animateStepTokenId == Pair(token.playerIndex, token.id),
                            cellSize = cellSize
                        )
                    } else {
                        // Multi tokens clustering (Mini-grid 2x2)
                        val miniCellSize = cellSize * 0.45f
                        val offsets = listOf(
                            Pair(-cellSize * 0.22f, -cellSize * 0.22f),
                            Pair(cellSize * 0.22f, -cellSize * 0.22f),
                            Pair(-cellSize * 0.22f, cellSize * 0.22f),
                            Pair(cellSize * 0.22f, cellSize * 0.22f)
                        )

                        tokens.forEachIndexed { idx, token ->
                            val (ox, oy) = offsets[idx % 4]
                            Box(
                                modifier = Modifier.offset(x = ox, y = oy)
                            ) {
                                LudoTokenPiece(
                                    token = token,
                                    isClickable = uiState.validMoves.contains(token.id) && uiState.currentPlayerIndex == token.playerIndex && !uiState.isGameOver,
                                    onClick = { onTokenClick(token.id) },
                                    isAnimating = false, // inhibit step bounce for secondary stacked tokens
                                    cellSize = miniCellSize
                                )
                            }
                        }
                    }
                }
            }

            // 4. Overlap player labels on yards matching the screenshot exactly!
            val p1Name = uiState.players.getOrNull(0)?.name ?: "Player1"
            val p2Name = uiState.players.getOrNull(1)?.name ?: "Player2"
            val p3Name = uiState.players.getOrNull(2)?.name ?: "Player3"
            val p4Name = uiState.players.getOrNull(3)?.name ?: "Player4"

            val p1Pct = getPlayerCompletionPercentage(0, uiState.tokens)
            val p2Pct = getPlayerCompletionPercentage(1, uiState.tokens)
            val p3Pct = getPlayerCompletionPercentage(2, uiState.tokens)
            val p4Pct = getPlayerCompletionPercentage(3, uiState.tokens)

            // Red Base (Top-Left): title at top-mid, percentage at bottom-mid of base area
            LudoBoardTextOverlay(text = p1Name, x = 0.dp, y = cellSize * 0.45f, cellSize = cellSize)
            LudoBoardTextOverlay(text = p1Pct, x = 0.dp, y = cellSize * 4.7f, cellSize = cellSize, color = Color(0xFFFED330))

            // Yellow Base (Top-Right): title at top-mid, percentage at bottom-mid of base area
            LudoBoardTextOverlay(text = p2Name, x = cellSize * 9, y = cellSize * 0.45f, cellSize = cellSize)
            LudoBoardTextOverlay(text = p2Pct, x = cellSize * 9, y = cellSize * 4.7f, cellSize = cellSize, color = Color(0xFFFED330))

            // Blue Base (Bottom-Right): percentage at top-mid, title at bottom-mid of base area
            LudoBoardTextOverlay(text = p3Pct, x = cellSize * 9, y = cellSize * 9.45f, cellSize = cellSize, color = Color(0xFFFED330))
            LudoBoardTextOverlay(text = p3Name, x = cellSize * 9, y = cellSize * 13.7f, cellSize = cellSize)

            // Green Base (Bottom-Left): percentage at top-mid, title at bottom-mid of base area
            LudoBoardTextOverlay(text = p4Pct, x = 0.dp, y = cellSize * 9.45f, cellSize = cellSize, color = Color(0xFFFED330))
            LudoBoardTextOverlay(text = p4Name, x = 0.dp, y = cellSize * 13.7f, cellSize = cellSize)
        }
    }
}

// Helper to draw a beautiful star shape using canvas Path
fun createStarPath(center: Offset, radius: Float): Path {
    val path = Path()
    val points = 5
    val step = (2 * kotlin.math.PI) / points
    val innerRadius = radius * 0.40f
    
    for (i in 0 until points) {
        val angle = i * step - (kotlin.math.PI / 2)
        val nextAngle = angle + (step / 2)
        
        val x1 = center.x + radius * kotlin.math.cos(angle).toFloat()
        val y1 = center.y + radius * kotlin.math.sin(angle).toFloat()
        val x2 = center.x + innerRadius * kotlin.math.cos(nextAngle).toFloat()
        val y2 = center.y + innerRadius * kotlin.math.sin(nextAngle).toFloat()
        
        if (i == 0) {
            path.moveTo(x1, y1)
        } else {
            path.lineTo(x1, y1)
        }
        path.lineTo(x2, y2)
    }
    path.close()
    return path
}

// Helper to draw player bases (Yards) with gradients and sockets
fun drawPlayerBase(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    pxCell: Float,
    baseTopLeft: Offset,
    mainColor: Color,
    gradientStart: Color,
    gradientEnd: Color,
    playerIdx: Int
) {
    val size = pxCell * 6
    // Outer rounded background card with rich dynamic gradient
    drawScope.drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(gradientStart, gradientEnd),
            start = baseTopLeft,
            end = Offset(baseTopLeft.x + size, baseTopLeft.y + size)
        ),
        topLeft = baseTopLeft,
        size = androidx.compose.ui.geometry.Size(size, size),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pxCell * 0.7f, pxCell * 0.7f)
    )

    // Inner gold/white gloss highlight border
    drawScope.drawRoundRect(
        color = Color.White.copy(alpha = 0.4f),
        topLeft = baseTopLeft,
        size = androidx.compose.ui.geometry.Size(size, size),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pxCell * 0.7f, pxCell * 0.7f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
    )

    // Glassmorphic inset container centered inside the yard
    val innerSize = pxCell * 4.6f
    val innerOffset = Offset(baseTopLeft.x + (size - innerSize) / 2f, baseTopLeft.y + (size - innerSize) / 2f)
    drawScope.drawRoundRect(
        color = Color.White.copy(alpha = 0.12f),
        topLeft = innerOffset,
        size = androidx.compose.ui.geometry.Size(innerSize, innerSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pxCell * 0.5f, pxCell * 0.5f)
    )
    drawScope.drawRoundRect(
        color = Color.White.copy(alpha = 0.25f),
        topLeft = innerOffset,
        size = androidx.compose.ui.geometry.Size(innerSize, innerSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pxCell * 0.5f, pxCell * 0.5f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
    )

    // Draw the 4 token sockets arranged in a 2x2 grid inside the yard (matching positions)
    val socketRelativeCoords = when (playerIdx) {
        0 -> listOf(Pair(2, 2), Pair(2, 3), Pair(3, 2), Pair(3, 3))
        1 -> listOf(Pair(2, 11), Pair(2, 12), Pair(3, 11), Pair(3, 12))
        2 -> listOf(Pair(11, 11), Pair(11, 12), Pair(12, 11), Pair(12, 12))
        3 -> listOf(Pair(11, 2), Pair(11, 3), Pair(12, 2), Pair(12, 3))
        else -> emptyList()
    }

    socketRelativeCoords.forEach { (r, c) ->
        val centerX = (c + 0.5f) * pxCell
        val centerY = (r + 0.5f) * pxCell
        val socketCenter = Offset(centerX, centerY)

        // Drop shadow
        drawScope.drawCircle(
            color = Color.Black.copy(alpha = 0.2f),
            radius = pxCell * 0.42f,
            center = socketCenter
        )

        // Rounded white button frame
        drawScope.drawCircle(
            color = Color.White,
            radius = pxCell * 0.38f,
            center = socketCenter
        )

        // Core inset primary color
        drawScope.drawCircle(
            color = mainColor,
            radius = pxCell * 0.32f,
            center = socketCenter
        )

        // Star vector engraving in the empty pocket (matching screenshot!)
        val starPath = createStarPath(socketCenter, pxCell * 0.16f)
        drawScope.drawPath(
            path = starPath,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

// Renders the static borders, homes, and colored trails
@Composable
fun LudoBoardBackgroundDrawing(cellSize: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val pxCell = cellSize.toPx()

        // 1. Draw Player Bases with gorgeous modern card designs & circular token pads (Yards)
        // Red base top-left
        drawPlayerBase(
            drawScope = this,
            pxCell = pxCell,
            baseTopLeft = Offset(0f, 0f),
            mainColor = LudoRed,
            gradientStart = Color(0xFFFF5252),
            gradientEnd = Color(0xFFC62828),
            playerIdx = 0
        )

        // Yellow base top-right
        drawPlayerBase(
            drawScope = this,
            pxCell = pxCell,
            baseTopLeft = Offset(pxCell * 9, 0f),
            mainColor = LudoYellow,
            gradientStart = Color(0xFFFFD54F),
            gradientEnd = Color(0xFFF57F17),
            playerIdx = 1
        )

        // Blue base bottom-right
        drawPlayerBase(
            drawScope = this,
            pxCell = pxCell,
            baseTopLeft = Offset(pxCell * 9, pxCell * 9),
            mainColor = LudoBlue,
            gradientStart = Color(0xFF42A5F5),
            gradientEnd = Color(0xFF0D47A1),
            playerIdx = 2
        )

        // Green base bottom-left
        drawPlayerBase(
            drawScope = this,
            pxCell = pxCell,
            baseTopLeft = Offset(0f, pxCell * 9),
            mainColor = LudoGreen,
            gradientStart = Color(0xFF66BB6A),
            gradientEnd = Color(0xFF1B5E20),
            playerIdx = 3
        )

        // 2. Draw 15x15 crisp rounded-edge tiles matching popular high-end design
        for (r in 0..14) {
            for (c in 0..14) {
                val isYard = (r in 0..5 && c in 0..5) || (r in 0..5 && c in 9..14) ||
                             (r in 9..14 && c in 0..5) || (r in 9..14 && c in 9..14)
                val isGoal = r in 6..8 && c in 6..8

                if (!isYard && !isGoal) {
                    val isStartCell = (r == 6 && c == 1) || (r == 1 && c == 8) || (r == 8 && c == 13) || (r == 13 && c == 6)
                    val isHomeStretch = (r == 7 && c in 1..5) || (r in 1..5 && c == 7) || (r == 7 && c in 9..13) || (r in 9..13 && c == 7)
                    val isSafeStarCell = (r == 8 && c == 2) || (r == 2 && c == 6) || (r == 6 && c == 12) || (r == 12 && c == 8)

                    val cellColor = when {
                        // Startup cell matching parent player color
                        r == 6 && c == 1 -> LudoRed
                        r == 1 && c == 8 -> LudoYellow
                        r == 8 && c == 13 -> LudoBlue
                        r == 13 && c == 6 -> LudoGreen

                        // Home paths are fully glossy colored tracks
                        r == 7 && c in 1..5 -> LudoRed
                        r in 1..5 && c == 7 -> LudoYellow
                        r == 7 && c in 9..13 -> LudoBlue
                        r in 9..13 && c == 7 -> LudoGreen

                        // Default path tile
                        else -> Color.White
                    }

                    // Padded rectangular area to achieve rounded modular grids
                    val pad = 1.3f
                    val sizePx = pxCell - 2 * pad
                    val left = c * pxCell + pad
                    val top = r * pxCell + pad

                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pxCell * 0.16f, pxCell * 0.16f)
                    )

                    // Draw a thin, solid, elegant cell border outline
                    val strokeColor = if (cellColor == Color.White) {
                        Color(0xFFE2E8F0)
                    } else {
                        Color.White.copy(alpha = 0.3f)
                    }
                    drawRoundRect(
                        color = strokeColor,
                        topLeft = Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pxCell * 0.16f, pxCell * 0.16f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )

                    // Overlay Safe Zone Star vectors on the canvas (No more emojis!)
                    if (isStartCell || isSafeStarCell) {
                        val starColor = when {
                            isStartCell -> Color.White
                            (r == 8 && c == 2) -> LudoRed
                            (r == 2 && c == 6) -> LudoYellow
                            (r == 6 && c == 12) -> LudoBlue
                            (r == 12 && c == 8) -> LudoGreen
                            else -> Color.White
                        }

                        val cellCenter = Offset(c * pxCell + pxCell / 2f, r * pxCell + pxCell / 2f)
                        val starPath = createStarPath(cellCenter, pxCell * 0.28f)
                        drawPath(
                            path = starPath,
                            color = starColor
                        )
                    }
                }
            }
        }

        // 3. Draw majestic central goal triangles pointing to center
        val goalLeft = pxCell * 6
        val goalTop = pxCell * 6
        val goalRight = pxCell * 9
        val goalBottom = pxCell * 9
        val center = Offset(pxCell * 7.5f, pxCell * 7.5f)

        // West triangle (Red)
        drawPath(
            path = Path().apply {
                moveTo(goalLeft, goalTop)
                lineTo(center.x, center.y)
                lineTo(goalLeft, goalBottom)
                close()
            },
            color = LudoRed
        )

        // North triangle (Yellow)
        drawPath(
            path = Path().apply {
                moveTo(goalLeft, goalTop)
                lineTo(center.x, center.y)
                lineTo(goalRight, goalTop)
                close()
            },
            color = LudoYellow
        )

        // East triangle (Blue)
        drawPath(
            path = Path().apply {
                moveTo(goalRight, goalTop)
                lineTo(center.x, center.y)
                lineTo(goalRight, goalBottom)
                close()
            },
            color = LudoBlue
        )

        // South triangle (Green)
        drawPath(
            path = Path().apply {
                moveTo(goalLeft, goalBottom)
                lineTo(center.x, center.y)
                lineTo(goalRight, goalBottom)
                close()
            },
            color = LudoGreen
        )

        // Highlight central diagonal dividing lines to look high-definition!
        val dividerColor = Color.White.copy(alpha = 0.45f)
        drawLine(color = dividerColor, start = Offset(goalLeft, goalTop), end = center, strokeWidth = 2f)
        drawLine(color = dividerColor, start = Offset(goalRight, goalTop), end = center, strokeWidth = 2f)
        drawLine(color = dividerColor, start = Offset(goalRight, goalBottom), end = center, strokeWidth = 2f)
        drawLine(color = dividerColor, start = Offset(goalLeft, goalBottom), end = center, strokeWidth = 2f)
    }
}

// Star symbols inside safe coordinates
@Composable
fun LudoBoardStarsDrawing(cellSize: androidx.compose.ui.unit.Dp) {
    // Left completely blank, stars are drawn as crystal vectors inside LudoBoardBackgroundDrawing for immaculate layout execution!
}

// Dynamic Token component
@Composable
fun LudoTokenPiece(
    token: TokenState,
    isClickable: Boolean,
    onClick: () -> Unit,
    isAnimating: Boolean,
    cellSize: androidx.compose.ui.unit.Dp
) {
    val tokenColor = when (token.playerIndex) {
        0 -> LudoRed
        1 -> LudoYellow
        2 -> LudoBlue
        3 -> LudoGreen
        else -> Color.Gray
    }

    // High accent pulsate loop for selectable moves
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_token")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Vertical bounce during step climbing animation
    val jumpTransition = rememberInfiniteTransition(label = "jump")
    val bounceFloat by jumpTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val currentScale = if (isClickable) pulseScale else 1f
    val currentYOffset = if (isAnimating) bounceFloat.dp else 0.dp

    Box(
        modifier = Modifier
            .offset(y = currentYOffset)
            .scale(currentScale)
            .size(cellSize * 0.85f)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .background(tokenColor, CircleShape)
            .border(
                width = if (isClickable) 2.5.dp else 1.5.dp,
                color = if (isClickable) GoldLegend else TextWhite.copy(alpha = 0.85f),
                shape = CircleShape
            )
            .clickable(enabled = isClickable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 3-D pawn model — transparent background so the player colour circle shows
        Pawn3DScene(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

// 3D physics dice simulator
@Composable
fun DiceAnimatorWidget(
    diceValue: Int,
    diceState: DiceState,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    
    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )

    val shakeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val isRolling = diceState == DiceState.ROLLING
    val currentRotate = if (isRolling) shakeRotation else 0f
    val currentScale = if (isRolling) shakeScale else if (isClickable) 1.05f else 1f

    val borderGlow = if (isClickable) {
        GoldLegend
    } else {
        SlateBorder
    }

    Box(
        modifier = Modifier
            .rotate(currentRotate)
            .scale(currentScale)
            .size(72.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = borderGlow)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(SlateCard, SlateBorder)
                )
            )
            .border(
                width = if (isClickable) 2.5.dp else 1.5.dp,
                color = borderGlow,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = isClickable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 3-D dice model — transparent bg lets the gradient box show through
        Dice3DScene(
            diceValue = diceValue,
            isRolling = isRolling,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
        )
    }
}

@Composable
fun DiceDotsOverlay(value: Int) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = 4.dp.toPx()
            val w = size.width
            val h = size.height

            val left = w * 0.25f
            val right = w * 0.75f
            val midX = w * 0.5f
            val top = h * 0.25f
            val bottom = h * 0.75f
            val midY = h * 0.5f

            val dotColor = GoldLegend

            when (value) {
                1 -> {
                    drawCircle(dotColor, r, Offset(midX, midY))
                }
                2 -> {
                    drawCircle(dotColor, r, Offset(left, top))
                    drawCircle(dotColor, r, Offset(right, bottom))
                }
                3 -> {
                    drawCircle(dotColor, r, Offset(left, top))
                    drawCircle(dotColor, r, Offset(midX, midY))
                    drawCircle(dotColor, r, Offset(right, bottom))
                }
                4 -> {
                    drawCircle(dotColor, r, Offset(left, top))
                    drawCircle(dotColor, r, Offset(right, top))
                    drawCircle(dotColor, r, Offset(left, bottom))
                    drawCircle(dotColor, r, Offset(right, bottom))
                }
                5 -> {
                    drawCircle(dotColor, r, Offset(left, top))
                    drawCircle(dotColor, r, Offset(right, top))
                    drawCircle(dotColor, r, Offset(midX, midY))
                    drawCircle(dotColor, r, Offset(left, bottom))
                    drawCircle(dotColor, r, Offset(right, bottom))
                }
                6 -> {
                    drawCircle(dotColor, r, Offset(left, top))
                    drawCircle(dotColor, r, Offset(right, top))
                    drawCircle(dotColor, r, Offset(left, midY))
                    drawCircle(dotColor, r, Offset(right, midY))
                    drawCircle(dotColor, r, Offset(left, bottom))
                    drawCircle(dotColor, r, Offset(right, bottom))
                }
            }
        }
    }
}

// Eti vs Rasel dramatic VS Intro
@Composable
fun EtiVsRaselBattleSplash(
    eti: PlayerState?,
    rasel: PlayerState?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            PremiumGameLogo(
                logoSize = 100.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SPECIAL MODE BATTLE",
                color = FlameOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Dual row representing fighter frames
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ETI FIGHTER CARD
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LegendAvatarWithFrame(
                        avatarUri = eti?.avatarUri,
                        frameAsset = eti?.frameAsset ?: "gold",
                        size = 90.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = eti?.name ?: "Eti",
                        color = LudoRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "RED COMMANDER", color = TextMuted, fontSize = 9.sp)
                }

                Spacer(modifier = Modifier.width(30.dp))

                // FLAMING VERSUS TAG
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(FlameOrange.copy(alpha = 0.2f), CircleShape)
                        .border(1.5.dp, FlameOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.width(30.dp))

                // RASEL FIGHTER CARD
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LegendAvatarWithFrame(
                        avatarUri = rasel?.avatarUri,
                        frameAsset = rasel?.frameAsset ?: "gold",
                        size = 90.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = rasel?.name ?: "Rasel",
                        color = LudoYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "YELLOW EMPEROR", color = TextMuted, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
            
            Text(
                text = "MAY THE LOGISTICS BE EVER IN YOUR FAVOR",
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CircularProgressIndicator(color = FlameOrange, modifier = Modifier.size(24.dp))
        }
    }
}

// Celebration Winner screen
private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun CelebrationToastBanner(winner: PlayerState?) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = GoldLegend)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SlateBg,
                        SlateCard
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GoldLegend, NeonCyan, GoldLegend)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glowing Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                LegendAvatarWithFrame(
                    avatarUri = winner?.avatarUri,
                    frameAsset = winner?.frameAsset ?: "gold",
                    size = 54.dp
                )
                // Small Crown on top right
                Text(
                    text = "👑",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-4).dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎉 VICTORY CHAMPION 🎉",
                    color = GoldLegend,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                
                Text(
                    text = "${winner?.name ?: "Legend player"} has moved all 4 pawns to center!",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Ludo Master Status Achieved",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun WinnerCelebrationOverlay(
    winner: PlayerState?,
    durationSec: Int,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val minStr = durationSec / 60
    val secStr = durationSec % 60
    val timeFormatted = String.format("%02d:%02d", minStr, secStr)

    // Falling Confetti Physics Particles State
    val confettiColors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFF00E5FF), // Neon Cyan
        Color(0xFFFF1744), // Crimson Red
        Color(0xFF39FF14), // Neon Green
        Color(0xFFFF007F), // Neon Pink
        Color(0xFFFFC107)  // Gold Yellow
    )

    var confettiList by remember {
        mutableStateOf(
            List(65) {
                ConfettiParticle(
                    x = (0..1080).random().toFloat(),
                    y = (-800..0).random().toFloat(),
                    vx = (-3..3).random().toFloat(),
                    vy = (5..15).random().toFloat(),
                    size = (15..32).random().toFloat(),
                    color = confettiColors.random(),
                    rotation = (0..360).random().toFloat(),
                    rotSpeed = (-6..6).random().toFloat(),
                    isCircle = (0..1).random() == 0
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            confettiList = confettiList.map { c ->
                var nextY = c.y + c.vy
                var nextX = c.x + c.vx
                val nextRot = (c.rotation + c.rotSpeed) % 360
                if (nextY > 2200f) {
                    nextY = -40f
                    nextX = (0..1080).random().toFloat()
                }
                c.copy(y = nextY, x = nextX, rotation = nextRot)
            }
        }
    }

    var showBanner by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400) // Visual entry timing delay
        showBanner = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Canvas Layer in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            confettiList.forEach { c ->
                val drawX = if (c.x < 0) 0f else if (c.x > width) width else c.x
                val drawY = c.y
                
                rotate(degrees = c.rotation, pivot = Offset(drawX, drawY)) {
                    if (c.isCircle) {
                        drawCircle(color = c.color, radius = c.size / 2f, center = Offset(drawX, drawY))
                    } else {
                        drawRect(
                            color = c.color,
                            topLeft = Offset(drawX - c.size / 2f, drawY - c.size / 2f),
                            size = androidx.compose.ui.geometry.Size(c.size, c.size / 2f)
                        )
                    }
                }
            }
        }

        // Sliding Celebratory Toast Banner at Top Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = showBanner,
                enter = slideInVertically(
                    initialOffsetY = { -it * 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                CelebrationToastBanner(winner = winner)
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 110.dp, bottom = 24.dp)
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Premium Game Logo as the Royal Seal of Victory
            PremiumGameLogo(
                logoSize = 110.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "VICTORY ROYAL",
                color = GoldLegend,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Winner panel
            LegendCard(borderColor = GoldLegend, modifier = Modifier.width(260.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LegendAvatarWithFrame(
                        avatarUri = winner?.avatarUri,
                        frameAsset = winner?.frameAsset ?: "none",
                        size = 72.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = winner?.name ?: "Legend Winner",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "RANK #1 ACHIEVED",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Match stats recap
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⌛ TIME", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = timeFormatted, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔥 STATUS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "COMPLETED", color = LudoGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action CTAs
            LegendButton(
                text = "PLAY MATCH AGAIN",
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(0.85f),
                isNeon = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onGoHome,
                colors = ButtonDefaults.buttonColors(containerColor = SlateBorder),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(text = "RETURN TO HEADQUARTERS", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MiniDiceAnimatorWidget(
    diceValue: Int,
    diceState: DiceState,
    isClickable: Boolean,
    colorAccent: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_shake")
    
    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mini_rotate"
    )

    val shakeScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mini_scale"
    )

    val isRolling = diceState == DiceState.ROLLING
    val currentRotate = if (isRolling) shakeRotation else 0f
    val currentScale = if (isRolling) shakeScale else if (isClickable) 1.08f else 1f

    val borderGlow = if (isClickable) colorAccent else SlateBorder

    Box(
        modifier = Modifier
            .rotate(currentRotate)
            .scale(currentScale)
            .size(size)
            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = borderGlow)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(SlateCard, SlateBorder)
                )
            )
            .border(
                width = if (isClickable) 2.5.dp else 1.25.dp,
                color = borderGlow,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isClickable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 3-D dice model shared engine — only 1 mini-dice shows at a time (current turn)
        Dice3DScene(
            diceValue = diceValue,
            isRolling = isRolling,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        )
    }
}

@Composable
fun LiveRecordDialog(
    uiState: LudoUIState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SlateBg)
                .border(2.dp, NeonCyan, RoundedCornerShape(24.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "📊 LIVE MATCH RECORDS",
                    color = GoldLegend,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // Key Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Match duration
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🕒 DURATION", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        val mins = uiState.matchDurationSec / 60
                        val secs = uiState.matchDurationSec % 60
                        Text(
                            text = String.format(java.util.Locale.US, "%02d:%02d", mins, secs),
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Total rolls
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎲 TOTAL ROLLS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.totalMatchDiceRolls}",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Total captures
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚔️ CAPTURES", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.totalMatchCaptures}",
                            color = LudoRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SlateBorder.copy(alpha = 0.4f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))
                
                // Player Status Standings Heading
                Text(
                    text = "CURRENT STANDINGS",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Active Players list showing status
                uiState.players.filter { it.isActive }.forEach { player ->
                    val colorAccent = when (player.colorIndex) {
                        0 -> LudoRed
                        1 -> LudoYellow
                        2 -> LudoBlue
                        3 -> LudoGreen
                        else -> TextWhite
                    }
                    
                    val pTokens = uiState.tokens.filter { it.playerIndex == player.colorIndex }
                    val homeCount = pTokens.count { it.isFinished }
                    val activeCount = pTokens.count { it.isOnTrack }
                    val yardCount = pTokens.count { it.isYard }
                    
                    val progressPct = getPlayerCompletionPercentage(player.colorIndex, uiState.tokens)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateCard.copy(alpha = 0.4f))
                            .border(1.dp, colorAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(colorAccent, CircleShape)
                            )
                            Text(
                                text = player.name,
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👑$homeCount  🏃$activeCount  📦$yardCount",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = progressPct,
                                color = colorAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SlateBorder.copy(alpha = 0.4f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))
                
                // Action log feed heading
                Text(
                    text = "RECENT MATCH FEEDS",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Action logs inside Dialog
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateCard.copy(alpha = 0.3f))
                        .border(1.dp, SlateBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    val dialogLogsState = rememberLazyListState()
                    LaunchedEffect(uiState.logMessages.size) {
                        if (uiState.logMessages.isNotEmpty()) {
                            dialogLogsState.animateScrollToItem(uiState.logMessages.size - 1)
                        }
                    }
                    LazyColumn(
                        state = dialogLogsState,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.logMessages) { log ->
                            Text(
                                text = "⚡ $log",
                                color = if (log.contains("captured")) LudoRed else if (log.contains("reached")) LudoGreen else TextGray,
                                fontSize = 10.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan, Color(0xFF00838F))
                            )
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLOSE RECORD BOARD",
                        color = SlateBg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
