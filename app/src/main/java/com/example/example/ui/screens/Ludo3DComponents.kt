package com.example.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.filament.Engine
import io.github.sceneview.Scene
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import kotlin.math.sin

// ─── Shared Filament engine across the whole game screen ───────────────────
// One Engine is expensive to create; sharing it across all dice/pawn scenes
// avoids creating 17+ independent Filament engines.

val LocalLudoEngine = staticCompositionLocalOf<Engine?> { null }
val LocalLudoModelLoader = staticCompositionLocalOf<ModelLoader?> { null }

/**
 * Wrap GameScreen content with this to provide a single shared
 * Filament Engine + ModelLoader for all 3-D composables.
 */
@Composable
fun LudoSceneProvider(content: @Composable () -> Unit) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    CompositionLocalProvider(
        LocalLudoEngine provides engine,
        LocalLudoModelLoader provides modelLoader,
        content = content
    )
}

// ─── Dice face rotation map ─────────────────────────────────────────────────
// Standard dice: opposite faces sum to 7, Y-up coordinate system.
// Tweak these values if your Dice.glb was modelled in a different orientation.
private fun getDiceFaceRotation(face: Int): Rotation = when (face) {
    1 -> Rotation(x =   0f, y = 0f, z =   0f)
    2 -> Rotation(x = -90f, y = 0f, z =   0f)
    3 -> Rotation(x =   0f, y = 0f, z = -90f)
    4 -> Rotation(x =   0f, y = 0f, z =  90f)
    5 -> Rotation(x =  90f, y = 0f, z =   0f)
    6 -> Rotation(x = 180f, y = 0f, z =   0f)
    else -> Rotation(x = 0f, y = 0f, z = 0f)
}

// ─── 3-D Dice Scene ─────────────────────────────────────────────────────────

/**
 * Renders Dice.glb from assets/models/.
 * • While rolling  → tumbles freely driven by onFrame (no Compose animation overhead).
 * • When stopped   → snaps to the correct face via a LaunchedEffect.
 * Background is transparent so the surrounding Box gradient shows through.
 */
@Composable
fun Dice3DScene(
    diceValue: Int,
    isRolling: Boolean,
    modifier: Modifier = Modifier
) {
    val engine = LocalLudoEngine.current ?: return
    val modelLoader = LocalLudoModelLoader.current ?: return

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0f, z = 3.5f)
    }
    val childNodes = rememberNodes()
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)

    // Use mutableStateOf so LaunchedEffect can read the node after onViewCreated fires
    val diceNodeRef = remember { mutableStateOf<ModelNode?>(null) }

    // rememberUpdatedState → onFrame lambda always reads the LATEST value
    // without being re-created on every recomposition
    val latestIsRolling = rememberUpdatedState(isRolling)
    val latestDiceValue = rememberUpdatedState(diceValue)

    // Snap to correct face when rolling stops
    LaunchedEffect(isRolling, diceValue) {
        if (!isRolling) {
            diceNodeRef.value?.rotation = getDiceFaceRotation(diceValue)
        }
    }

    Scene(
        modifier = modifier,
        engine = engine,
        view = view,
        modelLoader = modelLoader,
        collisionSystem = collisionSystem,
        isOpaque = false,
        cameraNode = cameraNode,
        childNodes = childNodes,
        onViewCreated = {
            val instance = modelLoader.createModelInstance("models/Dice.glb")
            val node = ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.3f
            ).apply {
                rotation = getDiceFaceRotation(latestDiceValue.value)
            }
            childNodes += node
            diceNodeRef.value = node
        },
        onFrame = { frameNanos ->
            if (latestIsRolling.value) {
                // Drive the tumble from the render loop — smooth, frame-rate independent
                val t = frameNanos / 1_000_000_000f
                diceNodeRef.value?.rotation = Rotation(
                    x = (t * 270f) % 360f,
                    y = (t * 190f) % 360f,
                    z = (t * 130f) % 360f
                )
            }
        }
    )
}

// ─── 3-D Pawn Scene ─────────────────────────────────────────────────────────

/**
 * Renders good_pawn.glb from assets/models/.
 * The pawn does a gentle idle sway so it feels alive even when standing still.
 * Background is transparent so the player-colour circle underneath shows through.
 */
@Composable
fun Pawn3DScene(modifier: Modifier = Modifier) {
    val engine = LocalLudoEngine.current ?: return
    val modelLoader = LocalLudoModelLoader.current ?: return

    val cameraNode = rememberCameraNode(engine) {
        // Slightly elevated camera gives a nice "looking down at the pawn" feel
        position = Position(x = 0f, y = 0.3f, z = 3.2f)
    }
    val childNodes = rememberNodes()
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)

    val pawnNodeRef = remember { mutableStateOf<ModelNode?>(null) }

    Scene(
        modifier = modifier,
        engine = engine,
        view = view,
        modelLoader = modelLoader,
        collisionSystem = collisionSystem,
        isOpaque = false,
        cameraNode = cameraNode,
        childNodes = childNodes,
        onViewCreated = {
            val instance = modelLoader.createModelInstance("models/good_pawn.glb")
            val node = ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.0f
            ).apply {
                position = Position(0f, -0.15f, 0f)
                rotation = Rotation(x = -5f, y = 0f, z = 0f)
            }
            childNodes += node
            pawnNodeRef.value = node
        },
        onFrame = { frameNanos ->
            // Gentle Y-axis sway: ±18° over ~2 s cycle
            val t = frameNanos / 1_000_000_000f
            val sway = (sin(t.toDouble()) * 18.0).toFloat()
            pawnNodeRef.value?.rotation = Rotation(x = -5f, y = sway, z = 0f)
        }
    )
}
